package org.flappy;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    static final int WINDOW_WIDTH = (int)(288*1.5f);
    static final int WINDOW_HEIGHT = (int)(512*1.5f);

    BufferedImage backgroundNight;
    BufferedImage backgroundDay;
    BufferedImage yellowBird;
    BufferedImage blueBird;
    BufferedImage redBird;
    BufferedImage topPipe;
    BufferedImage botPipe;

    Clip scoreSfx;
    Clip hitSfx;
    Timer gameLoop;
    Timer pipeTimer;
    Bird bird;
    java.util.List<Pipe> pipes;
    static final int birdX = WINDOW_WIDTH/8;
    static final int birdY = WINDOW_HEIGHT/2; // initial Y
    static final int birdW = (int)(34*1.5f);
    static final int birdH = (int)(24*1.5f);
    static final int PIPE_WIDTH = (int)(52*1.5f);
    static final int PIPE_HEIGHT =  (int)(320*1.5f);
    Random random;
    FlappyBird() {
        try {
            backgroundNight = ImageIO.read(getClass().getResource("background-night.png"));
            backgroundDay = ImageIO.read(getClass().getResource("background-day.png"));
            yellowBird = ImageIO.read(getClass().getResource("yellowbird.png"));
            blueBird = ImageIO.read(getClass().getResource("bluebird.png"));
            redBird = ImageIO.read(getClass().getResource("redbird.png"));
            topPipe = ImageIO.read(getClass().getResource("toppipe.png"));
            botPipe = ImageIO.read(getClass().getResource("botpipe.png"));
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getResource("point.wav").openStream());
            scoreSfx = AudioSystem.getClip();
            scoreSfx.open(audioStream);
            hitSfx = AudioSystem.getClip();
            hitSfx.open(AudioSystem.getAudioInputStream(getClass().getResource("hit.wav").openStream()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        }
        gameLoop = new Timer(1000/60, this);
        gameLoop.start();

        random = new Random(System.currentTimeMillis());
        bird = new Bird(birdX, birdY, birdW, birdH, Math.abs(random.nextInt() % 3));
        pipes = new LinkedList<>();
        pipeTimer = new Timer(4500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                var randint = random.nextInt() % 250;
                randint = Math.abs(randint);
                pipes.add(new Pipe(WINDOW_WIDTH, -randint, PIPE_WIDTH, PIPE_HEIGHT, true));
                pipes.add(new Pipe(WINDOW_WIDTH, PIPE_HEIGHT - randint + birdH* (randint % 4 + 4), PIPE_WIDTH, PIPE_HEIGHT, false));
            }
        });
        pipeTimer.start();
    }

    float bgAlpha = 0.0f;
    boolean gameOver = false;
    float score = 0.0f;
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Paint bg
        var g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bgAlpha));
        g.drawImage(backgroundDay, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, null);
        g2d.drawImage(backgroundNight, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, null);

        // Paint bird
        switch(bird.type) {
            case Bird.YELLOW_BIRD: {
                g.drawImage(yellowBird, bird.x, bird.y, bird.w, bird.h, null);
                break;
            }
            case Bird.BLUE_BIRD: {
                g.drawImage(blueBird, bird.x, bird.y, bird.w, bird.h, null);
                break;
            }
            case Bird.RED_BIRD: {
                g.drawImage(redBird, bird.x, bird.y, bird.w, bird.h, null);
                break;
            }
            default: {
                throw new RuntimeException("Nu se cunoaste tipul pasarii: " + bird.type);
            }
        }
        g2d.dispose();
        g2d = (Graphics2D) g.create();
        // paint pipes

        for(int i = 0; i < pipes.size(); i++) {
            var curPipe = pipes.get(i);
            if (curPipe.isTop) {
                g2d.drawImage(topPipe, curPipe.x, curPipe.y, curPipe.w, curPipe.h, null);
            } else {
                g2d.drawImage(botPipe, curPipe.x, curPipe.y, curPipe.w, curPipe.h, null);
            }
        }
        g2d.dispose();
        g.setFont(new Font("Arial", Font.BOLD, 32));
        g.drawString("Score: " + (int)score, WINDOW_WIDTH/20, WINDOW_HEIGHT/16);
    }

    private static void launchGame() {
        JFrame window = new JFrame();
        FlappyBird game = new FlappyBird();
        window.setFocusable(true);
        window.requestFocus();
//        window.setLocationRelativeTo(null);
        var dimensions = new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
        window.setResizable(false);
        window.setMinimumSize(dimensions);
        window.setMaximumSize(dimensions);
        window.setPreferredSize(dimensions);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.add(game);
        window.pack();
        window.addKeyListener(game);
        window.setVisible(true);
    }

    public static void main(String[] args) {
        launchGame();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }

    int birdVel = -15;
    static final int BIRD_MAX_SPEED = -20;
    int gravity = 1;
    private void update() {
        if (gameOver) {
            return;
        }
        dayNightTick();
        birdVel += gravity;
        bird.y += birdVel;
        if (bird.y < 0) {
            bird.y = 0;
        }
        else if (bird.y > WINDOW_HEIGHT) {
            gameOver = true;
        }

        for (var pipe : pipes) {
            pipe.x -= 2;
            if (pipe.x  == birdX && pipe.isTop) {
                addScore();
            }
            if (checkColision(pipe)) {
                gameOver = true;
                hitSfx.setFramePosition(0);
                hitSfx.start();
            }
        }
    }

    private boolean checkColision(Pipe pipe) {
        Rectangle rect1 = new Rectangle(bird.x, bird.y, bird.w, bird.h);
        Rectangle rect2 = new Rectangle(pipe.x, pipe.y, pipe.w, pipe.h);
        return rect1.intersects(rect2);
    }

    private void addScore() {
        score += 1;
        scoreSfx.setFramePosition(0); // rewind to beginning
        scoreSfx.start();
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    int jumpVel = -8;
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()){
            case KeyEvent.VK_SPACE: {
                    if (gameOver) {
                        gameOver = false;
                        bird.y = birdY;
                        birdVel = 1;
                        bird.type = Math.abs(random.nextInt()%3);
                        score = 0;
                        pipes = new ArrayList<>();
                        bgAlpha = 0.0f;
                    } else {
                        if (birdVel > BIRD_MAX_SPEED) {
                            birdVel += jumpVel;
                        }
                    }
                break;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { }

    static final float dayNightSpeed = 0.0006f;
    boolean isDay = true;
    private void dayNightTick() {
        if (isDay) {
            bgAlpha += dayNightSpeed;
            if (bgAlpha > 1.0f) {
                bgAlpha = 1.0f;
                isDay = false;
            }
        } else {
            bgAlpha -= dayNightSpeed;
            if (bgAlpha < 0.0f) {
                bgAlpha = 0.0f;
                isDay = true;
            }
        }
    }
}
