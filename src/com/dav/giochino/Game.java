package com.dav.giochino;
//package com.escapegame.main;

import java.awt.Canvas;
import java.util.Random;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.io.IOException;


public class Game extends Canvas implements Runnable{

    private static final long serialVersionUID = 155069109782341818L;


    public static final int WIDTH = 640, HEIGHT = WIDTH/12*9;

    private Thread thread;
    private boolean running = false;
    private Handler handler;
    private Random r;
    private HUD hud;
    private Spawner spawner;
    private Menu menu;
    private AudioPlayer audio;

    public static boolean pause;
    
    public enum STATE{
    	Menu, Game, Select, Help, End;
    }
    
    public static STATE gameState = STATE.Menu;

    public Game(){
    	
    	audio = new AudioPlayer();

    	pause = false;
    	
    	
        handler = new Handler();
        hud = new HUD();
        menu=new Menu(this,handler,this.hud);
        this.addKeyListener(new KeyInput(handler, this));
        this.addMouseListener(menu);
        new Window(WIDTH,HEIGHT, "Giochino!!",this);
        
        
        spawner = new Spawner(handler,hud);
        r = new Random();
        
        this.addKeyListener(new KeyInput(handler, this));
	
        if(gameState==STATE.Game) {
        	handler.clearEnemies();

        }else {
        	for (int i = 0; i < 10; i++) {
				
        	handler.addObject(new MenuParticle(r.nextFloat(WIDTH),r.nextFloat(HEIGHT),ID.MenuParticle,handler));
        	}
        }
		audio.update();
		//audio.repeat();

    }
    public synchronized void start(){
        thread = new Thread(this);
        thread.start();
        running=true;
    }

    public synchronized void stop(){
        try {
            thread.join();
            running = false;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run(){
    	this.requestFocus();
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000/amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;
        while(running){

            long now = System.nanoTime();
            delta+=(now-lastTime)/ns;
            lastTime = now;
            while(delta>=1){
                tick();
                delta--;
            }

            if(running){
                render();
            }
            frames++;
            if(System.currentTimeMillis() - timer > 1000){

                timer+=1000;
                System.out.println("FPS: "+ frames);
                frames =0;

            }

        }
        stop();

    }


    private void tick(){

    	if(!pause) {
			if(gameState==STATE.Game) {
				hud.tick();
				spawner.tick();			
		    	handler.tick();

				
				if(hud.HEALTH<=0) {
	
					handler.object.clear();
					gameState=STATE.End;
	
					
				}
				
			} else if((gameState==STATE.Menu)||(gameState==STATE.End)||(gameState==STATE.Select)) {
				menu.tick();	
		    	handler.tick();

			}
    	}
        
    }


    private void render(){
        BufferStrategy bs = this.getBufferStrategy();
        if(bs == null){
            this.createBufferStrategy(3);
            return;
        }

        //sfondo
        Graphics g = bs.getDrawGraphics();
        g.setColor(Color.black);
        g.fillRect(0,0,WIDTH,HEIGHT);
        
        handler.render(g);
        
        if(pause) {
        	g.setColor(Color.white);
        	g.drawString("Pausa", WIDTH/2-30, HEIGHT/2);
        	
        }
        
        
        if(gameState==STATE.Game) {
			hud.render(g);			
		} else if((gameState==STATE.Menu)||(gameState==STATE.Help)||(gameState==STATE.End)||(gameState==STATE.Select)) {
			menu.render(g);
			
		}


        g.dispose();
        bs.show();

       

    }

    public static float clamp(float var,float min,float max) {
    	if(var>=max)return var=max;
    	else if(var<=min)return var=min;
    	else return var;
    	
    }

    public static void main(String args[]){

        new Game();
    }
}