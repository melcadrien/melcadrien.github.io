/* SwordyPanel.Java
 * Jordan Arnold, 2012
 * 
 * This is the head of the game, every single process is sent through here and is processed and placed on the window.
 */

package mrSwordy;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.*;
import com.sun.j3d.utils.timer.J3DTimer;

//Sets up the window and starts up the engine.
//Handles the key inputs.
public class SwordyPanel extends JPanel implements Runnable {

	//set up static parameters
	//width and height are set up here and sent to the Game class
	private static final int WIDTH = 640;
	private static final int HEIGHT = 480;

	private static final int YIELD_POINT = 16;

	private static final int MAX_FRAME_SKIPS = 5;

	private static final String IMS_INFO = "Imgloader.txt";

	private double maxFPS;

	//Set up the basic methods
	private int gHeight;
	private int gWidth;
	private volatile boolean runningState;
	private volatile boolean pausedState;

	private long period;
	private Font font;

	//used for the graphics
	private Thread animator;

	private Graphics dbg; 
	private Image dbImage = null;
	private ImageLoader imsLoader;

	private BackgroundManager backgroundMan;
	private PlayableManager playableMan;
	private PlayerManager  playerMan;
	private Vector<EnemyManager> eMan;

	private double actualFPS = 0; 

	private long frameCount = 0;

	private static long MAX_STATS_INTERVAL = 1000000000L;
	//		private static long MAX_STATS_INTERVAL = 1000L;
	// record stats every 1 second (roughly)

	// used for gathering statistics
	private long statsInterval = 0L;    // in ns
	private long prevStatsTime;   
	private long gameStartTime;

	private long fpsBuffer = 0;

	private DecimalFormat df = new DecimalFormat("0.##");  // 2 dp

	private Swordy sTop;

	public SwordyPanel(int fps, Swordy top){
		maxFPS = fps + .5;

		this.gHeight = HEIGHT;
		this.gWidth = WIDTH;
		sTop = top;
		period = ((long)1000.0/(long)fps)*1000000L;
		pausedState = false;



		font = new Font("SansSerif", Font.BOLD, 24);
		setBackground(Color.white);
		setPreferredSize(new Dimension(gWidth, gHeight));
		setFocusable(true);  //Since Swordy.java is the activator, this line is needed for the panel to be focused for key commands.

		requestFocus();
		addKeyListener( new KeyAdapter() {
			public void keyPressed(KeyEvent e)
			{  processKey(e);  }
			public void keyReleased(KeyEvent e){
				int keyCode = e.getKeyCode();
				if(keyCode != KeyEvent.VK_UP){
					playerMan.stayStill();
					playableMan.stayStill();
					backgroundMan.stayStill();
				}
			}
		});  //Requires the previous line in order to work.

		imsLoader = new ImageLoader(IMS_INFO); 

		backgroundMan = new BackgroundManager(gWidth, gHeight, imsLoader);
		playableMan = new PlayableManager(gWidth, gHeight, imsLoader);
		playerMan = new PlayerManager(gWidth, gHeight, 2, playableMan, imsLoader, 100);

		loadEnemies(1);


	}

	private void processKey(KeyEvent e)
	// handles termination, help, and game-play keys
	{
		int keyCode = e.getKeyCode();

		// termination keys
		// listen for esc, q, end, ctrl-c on the canvas to
		// allow a convenient exit from the full screen configuration
		if ((keyCode == KeyEvent.VK_ESCAPE) || (keyCode == KeyEvent.VK_Q) ||
				(keyCode == KeyEvent.VK_END) ||
				((keyCode == KeyEvent.VK_C) && e.isControlDown()) )
			runningState = false;

		// game-play keys
		if (!pausedState) {
			// move the sprite and backgrounds based on key press.
			if (keyCode == KeyEvent.VK_LEFT) {
				playerMan.moveLeft(); 
				playableMan.moveRight();   //background and playable area moves the other way
				backgroundMan.moveRight();
			}
			else if (keyCode == KeyEvent.VK_RIGHT) {
				playerMan.moveRight();
				playableMan.moveLeft();
				backgroundMan.moveLeft();
			} 
			else if (keyCode == KeyEvent.VK_UP)
				playerMan.jump();
			else if(keyCode == KeyEvent.VK_R){
				playerMan.resetPos();
			}
		}
	}  // end of processKey()

	public void addNotify()
	// wait for the JPanel to be added to the JFrame before starting
	{ 
		super.addNotify();   // creates the peer
		beginGame();
	}


	//Threads the panel to allow thread sleeping.
	public void beginGame(){
		if (animator == null || !runningState) {
			animator = new Thread(this);
			animator.start();
		}
	}

	//Primary function
	public void run() {
		long sleepTime, beforeTime, afterTime, timeDiff;
		long overSleepTime = 0L;
		int currentYield = 0;
		long excessTime = 0L;

		gameStartTime = J3DTimer.getValue();
		prevStatsTime = gameStartTime;
		beforeTime = gameStartTime;

		runningState = true;

		while(runningState){
			gameUpdate();
			gameRender();
			paintScreen();
			//Actions done here.

			afterTime = J3DTimer.getValue();

			timeDiff = afterTime - beforeTime;
			//			extraTime = 
			sleepTime = ((period - timeDiff) - overSleepTime)+ fpsBuffer;
			//			System.out.println("TimeDiff " + timeDiff);

			//						System.out.println(sleepTime + " " + (sleepTime-fpsBuffer));
			if(sleepTime > 0){
				try{
					Thread.sleep(sleepTime/1000000L);//Delay in order to prevent it from constant rendering.
				}
				catch(InterruptedException e){}
				overSleepTime = (J3DTimer.getValue() - afterTime) - sleepTime;
				//						System.out.println(overSleepTime);
				//Note to self: Despite this program having less of a sleeptime than WormPanel, it has a longer lag time.
			}
			else{
				excessTime -= sleepTime;
				overSleepTime = 0L;

				currentYield += 1;
				if(currentYield >= YIELD_POINT){
					Thread.yield(); //To allow other threads to act.
					currentYield = 0;
				}
			}
			beforeTime = J3DTimer.getValue();

			int skips = 0;
			while((excessTime > period) && (skips < MAX_FRAME_SKIPS)) {
				//				System.out.println("excess");
				excessTime -= period;
				gameUpdate();    // update state but don't render
				skips++;
			}

			//			System.out.println(excessTime);

			storeCurrentFPS();
		}
		System.exit(0);
	}

	private void gameUpdate(){
		if (playerMan.willHitWall()) { // collision checking first
			playerMan.stayStill();    //stop movement
			playableMan.stayStill();
			backgroundMan.stayStill();
		}
		playerMan.updateSprite();
		//uses the current X of the player for shifting in the game field
		for(int i = 0; i < eMan.size(); i++){
			(eMan.get(i)).updateSprite(backgroundMan.getHead());
			playerMan.hitEnemy((eMan.get(i)).getMyRectangle());
		}
		backgroundMan.update(playerMan.getX());
		playableMan.update(playerMan.getX());
	}

	private void gameRender()
	{
		if (dbImage == null){
			dbImage = createImage(WIDTH, HEIGHT);
			if (dbImage == null) {
				System.out.println("dbImage is null");
				return;
			}
			else
				dbg = dbImage.getGraphics();
		}

		// clear the background
		dbg.setColor(Color.white);
		dbg.fillRect (0, 0, WIDTH, HEIGHT);

		dbg.setColor(Color.blue);
		dbg.setFont(font);

		//draw all the elements
		backgroundMan.display(dbg);
		playableMan.display(dbg);
		for(int i = 0; i < eMan.size(); i++)
			(eMan.get(i)).drawSprite(dbg);	
		playerMan.drawSprite(dbg);

		sTop.changeHeader(Math.round(actualFPS));
		
//		dbg.drawString("FPS: " + (Math.round(actualFPS)), 20, 25);  // was (10,55)

		dbg.setColor(Color.black);

	}  // end of gameRender()

	private void loadEnemies(int level){
		eMan = new Vector<EnemyManager>();
		eMan.add(new LRManager(gWidth, gHeight, 2, playableMan, imsLoader, 100, 400, 200));
		eMan.add(new LRAreaManager(gWidth, gHeight, 2, playableMan, imsLoader, 100, 300, 200));
		eMan.add(new FollowerManager(gWidth, gHeight, 2, playableMan, imsLoader, 100, 350, 200));
		eMan.add(new BirdManager(gWidth, gHeight, 2, playableMan, imsLoader, 100, 450, 200));
		eMan.add(new ArcherManager(gWidth, gHeight, 2, playableMan, imsLoader, 100, 500, 200));

	}

	private void paintScreen()
	// use active rendering to put the buffered image on-screen
	{ 
		Graphics g;
		try {
			g = this.getGraphics();
			if ((g != null) && (dbImage != null))
				g.drawImage(dbImage, 0, 0, null);
			Toolkit.getDefaultToolkit().sync();  // sync the display on some systems
			g.dispose();
		}
		catch (Exception e)
		{ System.out.println("Graphics error: " + e);  }
	} // end of paintScreen()

	//When the game is closed or finished.
	public void stopGame(){
		runningState = false;
	}
	//When the user is not on the window.
	public void pauseGame(){
		pausedState = true;
	}
	//When the user is on the window.
	public void resumeGame(){
		pausedState = false;
	}

	private void storeCurrentFPS(){ 

		frameCount++;
		statsInterval += period;
		double fpsMediator = 0;
		String fpsTo2;

		if (statsInterval >= MAX_STATS_INTERVAL) {     // store the current FPS
			long timeNow = J3DTimer.getValue();

			long realElapsedTime = timeNow - prevStatsTime;  //gets the time taken between the last time this method is called.

			//Here is where the FPS is being calculated.
			//Note that in order to get the optimal FPS we compare our current FPS with the nominal and adjust
			//to reach it.
			if (realElapsedTime > 0) {
				actualFPS = (((double)frameCount / realElapsedTime) * 1000000000L);
				fpsTo2 = df.format(actualFPS);
				if(Double.parseDouble(fpsTo2) >= maxFPS){ //We use the decimal placement to handle any overflow above the requiried FPS.
						fpsMediator = ((1000.0/((int)actualFPS) )*1000000L);
						fpsBuffer = (int)(((fpsMediator)%period));
//						System.out.println(fpsBuffer);
				}
			}

			frameCount = 0L;
			prevStatsTime = timeNow;
			statsInterval = 0L;   // reset
		}
	}  // end of storeStats()

}
