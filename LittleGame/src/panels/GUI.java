package panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.Timer;

import custom_ui_components.GraphicButton;
import game_object.Block;
import game_object.GameObject;
import game_object.Inventory;
import game_object.Player;
import main.AudioManager;
import main.FileManager;
import utils.KeyHandled;
import utils.PlayerController;
import world_utils.World;
import world_utils.WorldGenerator;

public class GUI extends JPanel implements KeyHandled, MouseListener, MouseWheelListener{
	
	private static final long serialVersionUID = 1L;
	private final int width, height, fps = 60, maxDistanceToEdge = 200;
	private PlayerController playerController;
	private Player player;
	private Inventory inventory;
	private WorldGenerator worldGenerator;
	private AudioManager audioManager = new AudioManager();
	
	private ArrayList<GameObject> world = World.getWorld();
	private ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
	
	public GUI(int width, int height, int worldGeneratorType, int worldSize, String needsPlayer) {
		this.width = width;
		this.height = height;
		this.setPreferredSize(new Dimension(width,height));
		
		worldGenerator = new WorldGenerator(worldGeneratorType, worldSize);
		
		if (World.getNewWorld()) {
			if (needsPlayer.equals("0")) {
				worldGenerator.startGeneration();
			}else {
				worldGenerator.startGeneration(player);
			}
		}
		
		mainTimer.start();
		
		addMouseListener(this);
		addMouseWheelListener(this);
		
		player = new Player(worldGenerator.getBlockSize(), worldGenerator.getBlockSize());
		player.setX(width/2-(player.getWidth()/2));
		player.setY(worldGenerator.getBlockSize()*4);
		
		playerController = new PlayerController(this, player);
		playerController.setGravity(true);
		
		inventory = World.inventory;
		
		addKeyListener(playerController);
	}
	
	Timer mainTimer = new Timer((int)(1000d/(double)fps), new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			repaint();
		}
	});
	
	public void addActionListener(ActionListener actionListener) {
		actionListeners.add(actionListener);
	}
	
	private void triggerActionListener() {
		FileManager.worldSave(World.getWorldName());
		World.resetWorld();
		stopAll();
		for (ActionListener actionListener: actionListeners) {
			actionListener.actionPerformed(new ActionEvent(this, 0, "close GUI"));
		}
	}
	
	private void stopAll() {
		mainTimer.stop();
		playerController.stopAll();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int halfWidth = width/2;
		int doubleWidth = width*2;
		int halfHeight = height/2;
		int doubleHeight = height*2;
		g.setColor(World.SKY);
		g.fillRect(0, 0, width, height);
		for (GameObject object: world) {
			if (Math.abs(object.getX()+(halfWidth))<doubleWidth && Math.abs(object.getY()+(halfHeight))<doubleHeight) {
				object.drawObject(g);
			}
		}
		player.drawObject(g);
		inventory.drawObject(g);
	}
	
	public void keyEvent(int keyCode) {
		if (keyCode == KeyEvent.VK_W) {
			for (GameObject i: world) {
				if (player.collideWith(i, 0)) {
					return;
				}
			}
			if (player.getY()>maxDistanceToEdge) {
				player.setY(player.getY()-1);
			}else {
				for (GameObject i: world) {
					i.setY(i.getY()+1);
				}
				World.setGlobalShiftY(World.getGlobalShiftY()-1);
			}
			player.setJump(player.getJump()-1);
			return;
		}
		if (keyCode == KeyEvent.VK_S) {
			for (GameObject i: world) {
				if (player.collideWith(i, 2)) {
					return;
				}
			}
			if (player.getY()+player.getHeight()<=height-maxDistanceToEdge) {
				player.setY(player.getY()+1);
			}else {
				for (GameObject i: world) {
					i.setY(i.getY()-1);
				}
				World.setGlobalShiftY(World.getGlobalShiftY()+1);
			}
			return;
		}
		if (keyCode == KeyEvent.VK_D) {
			for (GameObject i: world) {
				if (player.collideWith(i, 1)) {
					return;
				}
			}
			if (player.getX()+player.getWidth()<=width-maxDistanceToEdge) {
				player.setX(player.getX()+1);
			}else {
				for (GameObject i: world) {
					i.setX(i.getX()-1);
				}
				World.setGlobalShiftX(World.getGlobalShiftX()+1);
			}
			return;
		}
		if (keyCode == KeyEvent.VK_A) {
			for (GameObject i: world) {
				if (player.collideWith(i, 3)) {
					return;
				}
			}
			if (player.getX()>maxDistanceToEdge) {
				player.setX(player.getX()-1);
			}else {
				for (GameObject i: world) {
					i.setX(i.getX()+1);
				}
				World.setGlobalShiftX(World.getGlobalShiftX()-1);
			}
			return;
		}
		if (keyCode >= 49 && keyCode <= 57) {
			inventory.setSelectedSlot(keyCode-48);
		}
		if (keyCode == KeyEvent.VK_ESCAPE) {
			triggerActionListener();
		}
	}

	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			ArrayList<GameObject> removeable = new ArrayList<GameObject>();
			for (int i=0;i<world.size();i++) {
				if (world.get(i).getX()<e.getX() &&
						world.get(i).getX()+world.get(i).getWidth()>e.getX() &&
						world.get(i).getY()<e.getY() &&
						world.get(i).getY()+world.get(i).getHeight()>e.getY()) {
					removeable.add(world.get(i));
				}
			}
			for (GameObject gameObject: removeable) {
				audioManager.blockBreak();
				Color[] slots = inventory.getSlots();
				int[] slotAmounts = inventory.getSlotAmounts();
				boolean needNewSlot = true;
				boolean dontBreak = false;
				for (int i=0;i<slots.length;i++) {
					if (slots[i].equals(gameObject.getColor()) && slotAmounts[i] != 0 && slotAmounts[i] < 999) {
						slotAmounts[i] = slotAmounts[i]+1;
						needNewSlot = false;
						break;
					}
				}
				int ITS = 0;
				for (int i=0;i<slots.length;i++) {
					if (slotAmounts[i] == 0) {
						ITS++;
					}
				}
				if (ITS == 0) {
					dontBreak = true;
					needNewSlot = false;
				}
				if (needNewSlot) {
					int i;
					for (i=0;i<slotAmounts.length-1;i++) {
						if (slotAmounts[i] == 0) {
							break;
						}
					}
					slots[i] = gameObject.getColor();
					slotAmounts[i] = slotAmounts[i]+1;
				}
				inventory.setSlots(slots);
				inventory.setSlotAmounts(slotAmounts);
				if (!dontBreak) {
					world.remove(gameObject);
				}
			}
		}else if (e.getButton() == MouseEvent.BUTTON3) {
			int intialX = e.getX()-world.get(0).getX()%worldGenerator.getBlockSize();
			int intialY = e.getY()-world.get(0).getY()%worldGenerator.getBlockSize();
			int x = world.get(0).getX()%worldGenerator.getBlockSize()+(intialX-(intialX%worldGenerator.getBlockSize()));
			int y = world.get(0).getY()%worldGenerator.getBlockSize()+(intialY-(intialY%worldGenerator.getBlockSize()));
			for (int i=0;i<world.size();i++) {
				if (world.get(i).getX()<e.getX() &&
						world.get(i).getX()+world.get(i).getWidth()>e.getX() &&
						world.get(i).getY()<e.getY() &&
						world.get(i).getY()+world.get(i).getHeight()>e.getY()) {
					return;
				}
			}
			if (inventory.getSlotAmounts()[inventory.getSelectedSlot()-1] == 0) {
				return;
			}
			int[] slotAmounts = inventory.getSlotAmounts();
			slotAmounts[inventory.getSelectedSlot()-1] = slotAmounts[inventory.getSelectedSlot()-1]-1;
			inventory.setSlotAmounts(slotAmounts);
			Block block = new Block(worldGenerator.getBlockSize(), worldGenerator.getBlockSize(), inventory.getSlots()[inventory.getSelectedSlot()-1]);
			block.setX(x);
			block.setGlobalX(x+World.getGlobalShiftX());
			block.setY(y);
			block.setGlobalY(y+World.getGlobalShiftY());
			world.add(block);
		}
	}
	public void mouseReleased(MouseEvent e) {}
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWheelRotation() > 0) {
			inventory.setSelectedSlot(inventory.getSelectedSlot()+1);
		}else if (e.getWheelRotation() < 0) {
			inventory.setSelectedSlot(inventory.getSelectedSlot()-1);
		}
	}
}
