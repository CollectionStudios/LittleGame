package main;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class WorldGenerator {

	public static final int NORMAL_G = 0;
	
	public static final int SMALL_W = 100;
	public static final int MEDIUM_W = 500;
	public static final int LARGE_W = 1000;
	
	private int blockSize = 40;
	private ArrayList<Block> world = new ArrayList<Block>();
	
	private int generationType;
	private int worldSize;
	
	public WorldGenerator(int generationType, int worldSize) {
		this.generationType = generationType;
		this.worldSize = worldSize;
	}
	
	public void startGeneration() {
		if (generationType == NORMAL_G) {
			System.out.println("World Generation Started");
			int blockHeight = 0;
			for (int x=-worldSize/2;x<worldSize/2;x++) {
				for (int y=0;y<100-blockHeight;y++) {
					Block block = new Block(blockSize, blockSize);
					block.setX(x*blockSize);
					block.setY(750+(blockSize*(y+blockHeight)));
					if (y == 0) {
						block.setColor(new Color(126,255,51));
					}else if (y < 4) {
						block.setColor(new Color(181,143,76));
					}else {
						block.setColor(new Color(130,130,130));
					}
					world.add(block);
				}
				int random = (int)(Math.random()*3d);
				if (random == 0) {
					blockHeight--;
				}
				if (random == 2) {
					blockHeight++;
				}
			}
			System.out.println("World Generation Ended");
		}
	}
	
	public ArrayList<GameObject> getWorld() {
		ArrayList<GameObject> gameObjects = new ArrayList<GameObject>();
		for (Block block: world) {
			gameObjects.add(block);
		}
		return gameObjects;
	}
	
	public void paintWorld(Graphics g) {
		
	}
	
}
