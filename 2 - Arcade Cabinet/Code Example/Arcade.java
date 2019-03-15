package arcade;

import arcade.Moorhuhn.Moorhuhn;
import arcade.SkiSafari.SkiSafari;
import arcade.Pong.Pong;
import arcade.MarsMatrix.MarsMatrix;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.*;
import javax.swing.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier; 
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent; 
import gnu.io.SerialPortEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class Arcade implements KeyListener, SerialPortEventListener {

    JFrame wind;
    PaintPanel paintPanel;
    private Image dbImage;
    private Graphics dbg;
    GraphicsDevice myDevice;
    
    ArrayList<ArcadeGame> games;
    ArrayList<Image> gameIcon;
    Image defaultImage;
    int fps, runId, selection, startTimer, launchTimer, launchDuration, launchSrcX, launchSrcY, launchSrcW, launchSrcH, offset, offsetPos, offsetTransition, offsetTimer;
    int appW, appB, appH, appY;
    int dotD, dotW, dotStart;
    double startTransition, launchDX, launchX, launchDY, launchY, launchDW, launchW, launchDH, launchH;
    boolean gaming, launching, paused, minimized, menuStatic, displayFPS;
    
    boolean exitWind, restart;
    int exitSelection;
    final String[] exitOptions = {"Herunterfahren", "Standby", "ArcadeOs Neustart", "Abbrechen"};
    
    final int targetFPS = 60;
    final int maxGamesNr = 3; //Maximum Number of Apps displayed on screen
    final int maxOffsetTimer = 8; //Number of frames the transition between border-Apps takes

    //Arduino
    SerialPort serialPort;
    private static final String PORT_NAMES[] = {
        "COM1", "COM2", "COM3", "COM4", // Windows
        "/dev/ttyUSB0", // Linux
        "/dev/tty.usbserial-A9007UX1" // Mac OS X
    };
    private BufferedReader inputReader;
    private OutputStream output;
    private static final int TIME_OUT = 2000; //Milliseconds to block while waiting for port open
    private static final int DATA_RATE = 9600; //Default bits per second for COM port.
    ArrayList<ArduinoInput> input;
    ArrayList<Integer> inputStorage;
    boolean newInput;
    
    //Fonts
    Font ComicFontBase, ComicMedium, ComicHuge;
    Font AtariFontBase, AtariMedium;

    public static void main(String[] args) {
        Arcade arcade = new Arcade();
        if (System.getProperty("os.name").startsWith("Windows")) arcade.initSerialPort(); //dll runs only on Windows
        arcade.initArcade();
    }

    public void initArcade() {
        //Create a Window
        wind = new JFrame("Arcade");
        if (System.getProperty("os.name").equals("Linux")) {
            //Linux shows problems setting the Window into fullscreen
            wind.setSize(1000, 650);
            wind.setVisible(true);
        } else {
            wind.setUndecorated(true);
        }
        wind.setExtendedState(JFrame.MAXIMIZED_BOTH);
        wind.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        wind.setAlwaysOnTop(true);
        
        /*
        For real Fullscreen you should use the code below.
        However tests showed a drastic decrease in performance when doing so.
        Also the simple ExtendedState works mostly the same.
        
        myDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        myDevice.setFullScreenWindow(wind);
        */
        
        wind.setVisible(true);

        paintPanel = new PaintPanel();
        paintPanel.setSize(wind.getWidth(), wind.getHeight()); 
        //Paintpanel is added later on, to avoid entering the paint methode before all variables have been initiated.
        
        wind.addKeyListener(this);
        
        //------------------------------------------------------------------------------------------------------
        //Delete the cursor by replacing it with a transparent, 1 Pixel Image
        BufferedImage cursorImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
        wind.setCursor(blankCursor);
        
        //------------------------------------------------------------------------------------------------------
        //Arduino
        input = new ArrayList();
        inputStorage = new ArrayList();
        for (int i=0; i<16; i++) {
            input.add(new ArduinoInput(i+1)); 
        }
        newInput = false;

        //------------------------------------------------------------------------------------------------------
        //load Fonts
        try {
            //InputStream myStream = getClass().getResourceAsStream("/rsc/Fonts/KOMIKAX_.ttf");
            InputStream myStream = getClass().getResourceAsStream("/rsc/Fonts/SegoeUiLight.ttf");
            ComicFontBase = Font.createFont(Font.TRUETYPE_FONT, myStream);
            ComicMedium = ComicFontBase.deriveFont(Font.PLAIN, 40);
            ComicHuge = ComicFontBase.deriveFont(Font.BOLD, 120);
            
            myStream = getClass().getResourceAsStream("/rsc/Fonts/Atari.ttf");
            AtariFontBase = Font.createFont(Font.TRUETYPE_FONT, myStream);
            AtariMedium = AtariFontBase.deriveFont(Font.PLAIN, 40);
        } catch (Exception ex) {}

        //------------------------------------------------------------------------------------------------------
        //Set starting setup
        gaming = false;
        launching = false;
        paused = false;
        minimized = false;
        menuStatic = false;
        displayFPS = false;

        //Add all games 
        games = new ArrayList();
        games.add(new MarsMatrix(wind.getWidth(), wind.getHeight()));
        games.add(new Pong(wind.getWidth(), wind.getHeight()));
        games.add(new SkiSafari(wind.getWidth(), wind.getHeight())); //Nur experimentell, erst begonnen
        games.add(new Moorhuhn(wind.getWidth(), wind.getHeight())); //Nur experimentell, erst begonnen
        
        //------------------------------------------------------------------------------------------------------
        //Save the GameIcons to an ArrayList
        gameIcon = new ArrayList();
        for (int i = 0; i < games.size(); i++) {
            //Determine for every game if there is an Image available
            boolean noPic = false;
            try {
                if (games.get(i).AppImg() == null) noPic = true;
                else gameIcon.add(games.get(i).AppImg());
            } catch (Exception ex) {
                noPic = true;
            }
            if (noPic) {
                //If there is no image, create a default picture
                try { if (defaultImage == null) defaultImage = ImageIO.read(getClass().getResource("/rsc/DefaultImage.jpg"));
                } catch (Exception ex) {ex.printStackTrace();}
                
                BufferedImage logo = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
                Graphics g = logo.createGraphics();
                Graphics2D gbi = (Graphics2D) g;
                gbi.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                gbi.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                gbi.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                gbi.drawImage(defaultImage, 0, 0, 512, 512, wind);
                gbi.setColor(new Color(81,156,255));
                String str = games.get(i).title();
                for (int s=80; s<99; s-= 3) {
                    //Repeat until text fits. Then break out
                    Font AtariDefaultImage = AtariFontBase.deriveFont(Font.BOLD, s);
                    gbi.setFont(AtariDefaultImage);
                    FontMetrics fm = gbi.getFontMetrics();
                    Rectangle2D rect = fm.getStringBounds(str, gbi);
                    if (rect.getWidth() < 450) {
                        gbi.drawString(str, (int)(logo.getWidth()/2 - rect.getWidth()/2), (int)(logo.getHeight()*0.8 + rect.getHeight()/4));
                        break;
                    }
                }
                gameIcon.add(logo);
            }
        }
        
        //------------------------------------------------------------------------------------------------------
        //Set Parameters for the menu graphics and animations
        setAppParameters();
        selection = 0; offsetPos = 0; offsetTransition = 0; offsetTimer = 0; //parameters for showing wich game from the menu is selected and how big the offset has to be if more than the max. are shown. Transition and Timer for animations.
        runId = -1; //id of the game in the games-list that is running. -1 if no game is running
        startTimer = 0; //Setting it to 0 will trigger the starting-animation. Set to -1 to stop the animation
        launchTimer = 0; //Framenumber the flip-animation is currently at
        launchDuration = 35; //Number of frames the flip-animation takes when starting a game
        
        //------------------------------------------------------------------------------------------------------
        //Variables for the Exit window
        exitWind = false;
        restart = false;
        exitSelection = 0;

        //------------------------------------------------------------------------------------------------------
        //Add a ComponentListener to detect a resize.
        //In case of a resize, the ArcadeGames are informed and the appParameters are reloaded for correct display
        wind.add(paintPanel);
        wind.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent ce) {
                for (int i=0; i<games.size(); i++) {
                    games.get(i).resize(wind.getWidth(), wind.getHeight());
                    setAppParameters();
                }
            }

            @Override
            public void componentMoved(ComponentEvent ce) {}

            @Override
            public void componentShown(ComponentEvent ce) {}

            @Override
            public void componentHidden(ComponentEvent ce) {}
        });
        
        //------------------------------------------------------------------------------------------------------
        // The Gaming Loop. Repeat till exit
        // The Loop aims at 60fps (targetFPS)
        // If displayFPS is true, the FPS Rate is calculated once a second and displayed in the top-left corner
        
        long ts = System.currentTimeMillis();
        int frames = 0; 
        fps = 0;
        while (true) {
            long t = System.currentTimeMillis();

            runArcade();

            wind.repaint();

            long sleep = 1000 / targetFPS + t - System.currentTimeMillis();
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (Exception ex) {ex.printStackTrace();}
            }
            
            if (displayFPS) { //If activated, calculate the FSP-ratio
                frames++;
                if (System.currentTimeMillis() - ts > 1000) { //Looks how many frames were rendered during the last second
                    fps = frames;
                    frames = 0;
                    ts = System.currentTimeMillis();
                }
            }
        }
    }

    public void runArcade() {
        //------------------------------------------------------------------------------------------------------
        //Checks if an application-restart has been requested
        if (restart) {
            restartArcade();
        }
        
        //------------------------------------------------------------------------------------------------------
        //calls the arduinoInput Events
        //Resets released and typed buttons back to false
        for (ArduinoInput ai : input) {
            if (ai.released) {
                AIReleased(ai.TYPE);
                ai.released = false;
            } 
            if (ai.typed) {
                AITyped(ai.TYPE);
                ai.typed = false;
            }
            if (ai.pressed) {
                AIPressed(ai.TYPE);
            }
        }
        
        //------------------------------------------------------------------------------------------------------
        //If newInputs are available, call newSerialEvent and update the input-List
        if (newInput) {
            newInput = false;
            newSerialEvent();
        }

        //------------------------------------------------------------------------------------------------------
        //Checks the state of the games and runs their run-loop
        //Also checks and handles the starting and launching animations
        if (gaming && !paused) {
            games.get(runId).run();
            if (games.get(runId).finished()) { //if games are closed internally, they return finished==true and have to be closed
                games.get(runId).close();
                gaming = false;
                startTimer = 0; // setting startTimer to 0 triggers the starting-animation
            }
        } else {
            if (startTimer != -1) { // if startTimer is not -1, the starting animation is running (menu shifts to the left)
                startTimer++;
                startTransition = 400.0 / ((startTimer + 3) / 4.0) - 40; //Calculate menu position
                if (startTransition < 0) { //if smaller than 0, the animation is over.
                    startTransition = 0;
                    startTimer = -1;
                    menuStatic = true; //set menuStatic to increase renderquality, since FPS-drops aren't seen in a static image.
                }
            }
            if (launching) {
                launchTimer++;
                if (launchTimer > launchDuration) { //if the flip-animation is over
                    launching = false;
                    menuStatic = true; //set menuStatic to increase renderquality, since FPS-drops aren't seen in a static image.
                    launchTimer = 0;
                    if (games.get(selection).minimized() == false) {
                        games.get(selection).init(); //If game isn't minimized, meaning it's not initiated, call init()
                    } else {
                        paused = true; //if it's minimized, pause it when reopened
                    }
                    runId = selection; //set the runId two the game that has been started
                    gaming = true;
                }
            }

        }

    }
    
    public void closeArcade() {
        closeSerialPort(); 
        wind.dispose();
        System.exit(0);
    }
    
    public void restartArcade() {
        //close
        closeSerialPort();
        wind.dispose();
        
        //restart
        if (System.getProperty("os.name").startsWith("Windows")) initSerialPort(); //dll runs only on Windows
        initArcade();
    }

    public void initLaunch() {
        //------------------------------------------------------------------------------------------------------
        //Setting parameters for the flipping animation during game-launch
        launchTimer = 0;
        double sizeP = 0.1; //Size increment of the selected app

        //variables starting with launchD, describe the difference (velocity) between two frames.
        launchDX = (offsetPos + 1) * appB + offsetPos * appW - appW * sizeP;
        launchDY = appY - appH * sizeP;
        launchDW = wind.getWidth() - (appW * (1 + 2 * sizeP));
        launchDH = wind.getHeight() - (appH * (1 + 2 * sizeP));
        
        //Offset position/size of window
        launchX = 0;
        launchY = 0;
        launchW = 0;
        launchH = 0;
        
        //the launchSrc variables define the four corners of the image that have to be stretched along the flipping page.
        //Which points to take depends on the image ratio and where it was cropped to fit the page.
        double ratio1 = (appW * (1 + 2 * sizeP) - 40) / (appH * (1 + 2 * sizeP) - 100);
        double ratio2 = (double) gameIcon.get(selection).getWidth(null) / (double) gameIcon.get(selection).getHeight(null);
        if (ratio2 > ratio1) {
            launchSrcW = (int) (ratio1 * (double) gameIcon.get(selection).getHeight(null));
            launchSrcX = (int) ((gameIcon.get(selection).getWidth(null) - launchSrcW) / 2.0);
            launchSrcY = 0;
            launchSrcH = gameIcon.get(selection).getHeight(null);
        } else {
            launchSrcH = (int) ((double) gameIcon.get(selection).getWidth(null) / ratio1);
            launchSrcY = (int) ((gameIcon.get(selection).getHeight(null) - launchSrcH) / 2.0);
            launchSrcX = 0;
            launchSrcW = gameIcon.get(selection).getWidth(null);
        }
    }
    
    public void setAppParameters() {
        //Set Parameters for the menu graphics and animations
        //Main purpose is to save calculation time by doing jobs only once that don't need to be done for every frame.
        
        //App-Dots
        dotD = 7; dotW = 25; dotStart = (int)(wind.getWidth()/2 - (games.size()-1.5)/2.0*(dotD+dotW));
        
        //App width and position
        if (games.size() < maxGamesNr) {
            appW = (int) (4.0000 / (5 * games.size()) * wind.getWidth());
            appB = (int) (1.0000 / (5 * (games.size() + 1)) * wind.getWidth());
        } else {
            appW = (int) (4.0000 / (5 * maxGamesNr) * wind.getWidth());
            appB = (int) (1.0000 / (5 * (maxGamesNr + 1)) * wind.getWidth());
        }
        appH = (int)(wind.getHeight()*0.5);
        appY = (wind.getHeight()-appH)/2;
    }

    // Paint
    public class PaintPanel extends JPanel {

        public void paint(Graphics g) {
            if (gaming) {
                // If a game is running, give the Rendering task to it.
                games.get(runId).paint(g);
            } else {
                // If no game is running, paint the menu
                
                //------------------------------------------------------------------------------------------------------
                // Create the g2d element and set the rendering options
                // Note: options have heavy influence on performance
                // When the menu is static (not moving), the quality is increased, since slower FPS aren't visible
                // For the animations interpolation is removed for more fluency
                Graphics2D g2d = (Graphics2D) g;
                if(menuStatic) {
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                } else {
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
                }

                //------------------------------------------------------------------------------------------------------
                // Transition when entering menu
                // Sets color and paints background
                if (startTimer != -1 && startTimer < 15) {
                    if (runId != -1) {
                        games.get(runId).paint(g);
                    }
                    g2d.setColor(new Color(60, 60, 60, 17 * startTimer));
                } else {
                    g2d.setColor(new Color(60, 60, 60));
                }
                g2d.setFont(ComicMedium);
                g2d.fillRect(0, 0, wind.getWidth(), wind.getHeight());

                //------------------------------------------------------------------------------------------------------
                // Offset is used when more than max available apps on screen are used.
                // When moving to the right, the offset has to be increased, including an animation
                int launchId = 0;
                offset = -(selection-offsetPos)*(appW+appB);
                if (offsetTimer != 0) {
                    offset += offsetTransition*offsetTimer/maxOffsetTimer;
                    offsetTimer--;
                }
                
                //------------------------------------------------------------------------------------------------------
                //Displaying games
                for (int i = 0; i < games.size(); i++) {
                    String str = games.get(i).title();
                    FontMetrics fm = g2d.getFontMetrics();
                    Rectangle2D rect = fm.getStringBounds(str, g2d);

                    //Increase the size of the currently selected game
                    double sizeP = 0;
                    if (selection == i) {
                        sizeP = 0.1;
                    }

                    //If the actual game is being launched, skip it and paint later. Else paint regularly
                    if (launching && i == selection) {
                        launchId = i;
                    } else {
                        g2d.setColor(Color.LIGHT_GRAY);
                        g2d.fillRoundRect((int) ((i + 1) * appB + i * appW - appW * sizeP + startTransition + offset), (int) (appY - appH * sizeP), (int) (appW * (1 + 2 * sizeP)), (int) (appH * (1 + 2 * sizeP)), 10, 10);
                        g2d.setColor(Color.BLACK);
                        g2d.drawString(str, (int) ((i + 1) * appB + i * appW - appW * sizeP + (appW * (1 + 2 * sizeP)) / 2 - rect.getWidth() / 2 + startTransition + offset), (int) (appY + (appH * (1 + sizeP)) - 20));

                        int imgX = (int) ((i + 1) * appB + i * appW - appW * sizeP + startTransition + offset + 20);
                        int imgY = (int) (appY - appH * sizeP + 20);
                        int imgW = (int) (appW * (1 + 2 * sizeP) - 40);
                        int imgH = (int) (appH * (1 + 2 * sizeP) - 100);
                        
                        //The ratios are necessary to decide wich part of the gameIcon to crop, since all Image ratios are accepted and have to be squares in the end
                        double ratio1 = (double) imgW / (double) imgH;
                        double ratio2 = (double) gameIcon.get(i).getWidth(this) / (double) gameIcon.get(i).getHeight(this);
                        if (ratio2 > ratio1) {
                            int srcW = (int) (ratio1 * (double) gameIcon.get(i).getHeight(this));
                            int srcX = (int) ((gameIcon.get(i).getWidth(this) - srcW) / 2.0);
                            g2d.drawImage(gameIcon.get(i), imgX, imgY, imgX + imgW, imgY + imgH, srcX, 0, srcX + srcW, gameIcon.get(i).getHeight(this), this);
                        } else {
                            int srcH = (int) ((double) gameIcon.get(i).getWidth(this) / ratio1);
                            int srcY = (int) ((gameIcon.get(i).getHeight(this) - srcH) / 2.0);
                            g2d.drawImage(gameIcon.get(i), imgX, imgY, imgX + imgW, imgY + imgH, 0, srcY, gameIcon.get(i).getWidth(this), srcY + srcH, this);
                        }
                    }
                }
 
                //------------------------------------------------------------------------------------------------------
                //Display flipping page on a game-launch
                //Uses variables defined in initLaunch() to save calculation time
                if (launching) {
                    launchX += launchDX / launchDuration;
                    launchY += launchDY / launchDuration;
                    launchW += launchDW / launchDuration;
                    launchH += launchDH / launchDuration;
                    double sizeP = 0.1;
                    int pivot = (int) ((launchId + 1) * appB + launchId * appW - appW * sizeP - launchX + (appW * (1 + 2 * sizeP) + launchW) / 2 + offset);
                    int defW = (int) (Math.abs(Math.cos(Math.toRadians((double) launchTimer / (double) launchDuration * 180.0))) * (appW * (1 + 2 * sizeP) + launchW));

                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.fillRoundRect(pivot - defW / 2, (int) (appY - appH * sizeP - launchY), defW, (int) (appH * (1 + 2 * sizeP) + launchH), 10, 10);
                    if ((double) launchTimer / (double) launchDuration * 180 < 90) {
                        int imgY = (int) (appY - appH * sizeP + 20 - launchY);
                        int imgW = (int) (Math.abs(Math.cos(Math.toRadians((double) launchTimer / (double) launchDuration * 180.0))) * (appW * (1 + 2 * sizeP) - 40 + launchW));
                        int imgH = (int) (appH * (1 + 2 * sizeP) - 100 + launchH);
                        g2d.drawImage(gameIcon.get(launchId), pivot - imgW / 2, imgY, pivot + imgW / 2, imgY + imgH, launchSrcX, launchSrcY, launchSrcX + launchSrcW, launchSrcY + launchSrcH, this);
                    } else {
                        //Code to display game at the back of the flipping page
                        //Issues in performance and stretching.
                        
//                        BufferedImage buffImg = new BufferedImage(wind.getWidth(), wind.getHeight(), BufferedImage.TYPE_INT_ARGB);
//                        Graphics gbi = buffImg.createGraphics();
//                        games.get(selection).paint(gbi);
//
//                        int imgY = (int) (y - h * sizeP - launchY);
//                        int imgW = (int) (Math.abs(Math.cos(Math.toRadians((double) launchTimer / (double) launchDuration * 180.0))) * (w * (1 + 2 * sizeP) + launchW));
//                        int imgH = (int) (h * (1 + 2 * sizeP) + launchH);
//                        g2d.drawImage(buffImg, pivot - defW / 2, (int) (y - h * sizeP - launchY), defW, (int) (h * (1 + 2 * sizeP) + launchH), this);
                    }
                }
                
                //------------------------------------------------------------------------------------------------------
                //Paint the game-dots
                if (menuStatic) {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                }
                g2d.setColor(Color.LIGHT_GRAY);
                for (int i=0; i<games.size(); i++) {
                    if (i == selection) {
                        int bigDotD = (int)(dotD*1.8);
                        g2d.fillOval(dotStart + i*dotW - (bigDotD/2), (int)(wind.getHeight()*0.9 - bigDotD/2), bigDotD, bigDotD);
                    } else {
                        g2d.fillOval(dotStart + i*dotW - dotD/2, (int)(wind.getHeight()*0.9 - dotD/2), dotD, dotD);
                    }
                }
                
                //------------------------------------------------------------------------------------------------------
                //Paint exit window
                if (exitWind) {
                    g2d.setColor(new Color(30, 30, 30, 150));
                    g2d.fillRect(0, 0, wind.getWidth(), wind.getHeight());
                    
                    g2d.setColor(new Color(200,200,200));
                    g2d.fillRoundRect((int)(wind.getWidth()*0.3), (int)(wind.getHeight()*0.3), (int)(wind.getWidth()*0.4), (int)(wind.getHeight()*0.4), wind.getHeight()/75, wind.getHeight()/75);
                    
                    int space = (int)(wind.getHeight()*0.4 / (exitOptions.length + 2));
                    
                    g2d.setColor(new Color (100, 150, 220));
                    g2d.fillRect((int)(wind.getWidth()*0.35), (int)(wind.getHeight()*0.3 + (exitSelection+1)*space), (int)(wind.getWidth()*0.3), space);
                    
                    g2d.setFont(ComicMedium);
                    for (int i=0; i<exitOptions.length; i++) {
                        g2d.setColor(Color.BLACK);
                        FontMetrics fm = g2d.getFontMetrics();
                        Rectangle2D rect = fm.getStringBounds(exitOptions[i], g2d);
                        g2d.drawString(exitOptions[i], wind.getWidth()/2 - (int)rect.getWidth()/2, (int)(wind.getHeight()*0.3 + (i+2)*space - rect.getHeight()/2));
                    }
                }
            }
            
            //------------------------------------------------------------------------------------------------------
            //Overlay semitransparent pause-screen
            if (paused) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                Color p = new Color(30, 30, 30, 100);
                g2d.setColor(p);
                g2d.fillRect(0, 0, wind.getWidth(), wind.getHeight());
                g2d.setColor(Color.BLACK);
                g2d.setFont(ComicMedium);
                String str = "Pause";
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D rect = fm.getStringBounds(str, g2d);
                g2d.drawString(str, (int) (wind.getWidth() / 2 - rect.getWidth() / 2), wind.getHeight() / 2);
            }
            
            //------------------------------------------------------------------------------------------------------
            //If activated, display FPS-Rate
            if (displayFPS) {
                g.setColor(Color.GREEN);
                g.setFont( new Font( "Courier New", Font.PLAIN, 14 ) );
                g.drawString("FPS: " + fps, 20, 20);
            }
        }

    }

    // Input
    public void AIPressed(int e) {
        if (gaming) {
            games.get(runId).AIPressed(e);
        }
    }
    
    public void AIReleased(int e) {
        if (gaming) {
            games.get(runId).AIReleased(e);
        }
    }
    
    public void AITyped(int e) {
        if (gaming) {
            if (e == ArduinoInput.TYPE_PAUSE) {
                paused = !paused;
            } else if (e == ArduinoInput.TYPE_MINIMIZE) {
                games.get(runId).minimize();
                gaming = false;
                paused = false;
                startTimer = 0;
                menuStatic = false;
            } else if (e == ArduinoInput.TYPE_CLOSE) {
                games.get(runId).close();
                gaming = false;
                paused = false;
                startTimer = 0;
                menuStatic = false;
            } else {
                games.get(runId).AITyped(e);
            }
        } else if (launching == false) {
            if (exitWind) { //If the exit-window is open
                if (e == ArduinoInput.TYPE_P1_BUTTON1 || e == ArduinoInput.TYPE_P2_BUTTON1) {
                    if (exitSelection == 0) {
                        //Shutdown
                        try {
                            Runtime.getRuntime().exec("shutdown -s -t 3");
                        } catch (Exception ex) {}
                        closeArcade();
                    } else if (exitSelection == 1) {
                        //standby
                        exitWind = false;
                        exitSelection = 0;
                        try {
                            Runtime.getRuntime().exec("Rundll32.exe powrprof.dll,SetSuspendState Sleep");
                        } catch (IOException ex) {}
                    } else if (exitSelection == 2) {
                        //Restart ArcadeOs once back in the main-thread
                        restart = true;
                    } else if (exitSelection == 3) {
                        //close and cancel exit-window
                        exitWind = false;
                        exitSelection = 0;
                    }
                } else if (e == ArduinoInput.TYPE_P1_UP || e == ArduinoInput.TYPE_P2_UP) {
                    if (exitSelection > 0) exitSelection--;
                    else exitSelection = exitOptions.length-1;
                } else if (e == ArduinoInput.TYPE_P1_DOWN || e == ArduinoInput.TYPE_P2_DOWN) {
                    if (exitSelection < exitOptions.length-1) exitSelection++;
                    else exitSelection = 0;
                }
                
            } else {
                if (e == ArduinoInput.TYPE_P1_LEFT ||e == ArduinoInput.TYPE_P2_LEFT) {
                    if (selection <= 0) {
                        //selection = games.size() - 1;
                        //offsetPos = 2;
                    } else {
                        selection--;
                        if (offsetPos == 0) {
                            offsetTransition = -(appW+appB);
                            offsetTimer = maxOffsetTimer;
                        } else offsetPos--;
                    }
                } else if (e == ArduinoInput.TYPE_P1_RIGHT || e == ArduinoInput.TYPE_P2_RIGHT) {
                    if (selection >= games.size()-1) {
                        //selection = 0;
                        //offsetPos = 0;
                    } else {
                        selection++;
                        if (offsetPos == maxGamesNr-1) {
                            offsetTransition = appW+appB;
                            offsetTimer = maxOffsetTimer;
                        } else offsetPos++;
                    }
                } else if (e == ArduinoInput.TYPE_P1_BUTTON1 || e == ArduinoInput.TYPE_P2_BUTTON1) {
                    launching = true;
                    menuStatic = false;
                    initLaunch();
                    if (games.get(selection).minimized() == false) {
                          //Code necessary to show game on second side of flipping page
    //                    games.get(selection).init();
    //                    BufferedImage buffImg = new BufferedImage(wind.getWidth(), wind.getHeight(), BufferedImage.TYPE_INT_ARGB);
    //                    Graphics2D gbi = buffImg.createGraphics();
    //                    games.get(selection).paint(gbi);
                    }
                } else if (e == ArduinoInput.TYPE_CLOSE || e == ArduinoInput.TYPE_MINIMIZE ||e == ArduinoInput.TYPE_PAUSE) {
                    exitWind = true;
                }
            }
        }
    }
    
    public void keyTyped(KeyEvent e) {
        if (gaming) {
            games.get(runId).keyTyped(e);
        } else {
            if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                AITyped(ArduinoInput.TYPE_P1_BUTTON1);
            }
        }    
        if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
            closeArcade();
        }
    }

    public void keyPressed(KeyEvent e) {
        //First standardized buttons p, m and c
        if (e.getKeyCode() == KeyEvent.VK_P) {
            AITyped(ArduinoInput.TYPE_PAUSE);
        } else if (e.getKeyCode() == KeyEvent.VK_M) {
            AITyped(ArduinoInput.TYPE_MINIMIZE);
        } else if (e.getKeyCode() == KeyEvent.VK_C) {
            AITyped(ArduinoInput.TYPE_CLOSE);
        } else { //Then all other cases
            if (gaming) {
                games.get(runId).keyPressed(e);
            } else if (launching == false && exitWind == false) {

                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    if (selection <= 0) {
                        //selection = games.size() - 1;
                        //offsetPos = 2;
                    } else {
                        selection--;
                        if (offsetPos == 0) {
                            offsetTransition = -(appW+appB);
                            offsetTimer = maxOffsetTimer;
                        } else offsetPos--;
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if (selection >= games.size()-1) {
                        //selection = 0;
                        //offsetPos = 0;
                    } else {
                        selection++;
                        if (offsetPos == maxGamesNr-1) {
                            offsetTransition = appW+appB;
                            offsetTimer = maxOffsetTimer;
                        } else offsetPos++;
                    }
                }
            } else if (exitWind) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if (exitSelection > 0) exitSelection--;
                    else exitSelection = exitOptions.length-1;
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (exitSelection < exitOptions.length-1) exitSelection++;
                    else exitSelection = 0;
                }
            }

            //Set displayFPS on and off
            if (e.getKeyCode() == KeyEvent.VK_F1) {
                displayFPS = !displayFPS;
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        if (gaming) {
            games.get(runId).keyReleased(e);
        }
    }
    
    //Arduino
    public void initSerialPort() {
        //Most of the code in this method comes from the Arduino and Java site:
        //http://playground.arduino.cc/Interfacing/Java
        
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        //First, Find an instance of serial port as set in PORT_NAMES.
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            for (String portName : PORT_NAMES) {
                if (currPortId.getName().equals(portName)) {
                    portId = currPortId;
                    break;
                }
            }
        }
        if (portId == null) {
            System.out.println("Could not find COM port.");
            return;
        }

        try {
            // open serial port, and use class name for the appName.
            serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            // open the streams
            inputReader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            //output = serialPort.getOutputStream();

            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }
    
    public synchronized void closeSerialPort() {
        // Closes the serial Port
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }
    
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                /*
                In this segment, the new input is read.
                To avoid exceptions, for example changing inputs during loops, the inputs are stored in the inputStorage and then added in the next run loop.
                This will trigger newSerialEvent in the next loop.
                */
                if (inputReader.ready()) {
                    String inputLine = inputReader.readLine();
                    
                    inputStorage.clear();
                    String binaryString = Long.toBinaryString(Long.parseLong(inputLine)); //first, interprete the input Line as a number (Long), then convert it into a binaryString, consisting out of 0 and 1.
                    char[] binaryChars = binaryString.toCharArray(); //Divide the String into single Characters
                    for (int i=0; i<binaryChars.length; i++) {
                        inputStorage.add(Integer.parseInt(Character.toString(binaryChars[binaryChars.length - (i+1)]))); //Add each input as an Integer into the inputStorage
                    }
                    while (inputStorage.size() < 16) {
                        //Since it's a number, zeros in front of the first 1 will be deleted. Here they are added again.
                        inputStorage.add(0);
                    }
                    //Set newInput to true, to add the inputStorage in the next loop
                    newInput = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public synchronized void newSerialEvent() {
        /*
        The new inputs in the inputStorage are now added.
        Pressed: return true if button is pressed - Several times.
        Released: return true if button is released - only once.
        Typed: return true the first time it is pressed - only once.
        */
        for (int i=0; i<input.size(); i++) {
            if (inputStorage.get(i) == 1) {
                if (input.get(i).pressed == false) {
                    input.get(i).typed = true;
                    input.get(i).pressed = true;
                }
            } else {
                if (input.get(i).pressed) {
                    input.get(i).released = true;
                }
                input.get(i).pressed = false;
            }
        }
    }

}
