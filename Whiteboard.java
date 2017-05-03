import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javafx.scene.layout.VBox;

public class Whiteboard extends JFrame {
	
	
	
	private Canvas canvas; 
	private JFrame board;
	private JTextField textField; 
	private JScrollPane scrollpane;
	private JComboBox fontSelector; 
	private TableModel tableModel;
	private JTable table;
	private HashMap<String, Integer> fontMap; 
	private DShape selectedShape;
	private JButton serverMode, clientMode;
	private ServerStarter serverStarter;
	
	private int mode;
	private static int normal = 0;
	private static int server = 1;
	private static int client = 2;
	
	public Whiteboard() { 
        
		board = new JFrame("Whiteboard");
		
		setLayout(new BorderLayout()); 
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        
        canvas = new Canvas(this);
        mode = normal;
        
        JButton addOval = new JButton("Add Oval");
		addOval.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DOvalModel model = new DOvalModel(25,25,100,100,Color.gray);
				canvas.addShape(model);
				canvas.repaint();
				
			}
		});
		
		JButton addRectangle = new JButton("Add Rectangle");
		addRectangle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DRectModel model = new DRectModel(25,25,100,100,Color.gray);
				canvas.addShape(model);
				canvas.repaint();
				
			}
		});
		
		JButton addLine = new JButton("Add Line");
		addLine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Point p1 = new Point(25,25);
				Point p2 = new Point(75,75);
				DLineModel model = new DLineModel(p1,p2);
				//model.setPoints(p1,p2);
				canvas.addShape(model);
				canvas.repaint();
			}
		});
		
		JButton addText = new JButton("Add Text");
		addText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = textField.getText();
				DTextModel model = new DTextModel();
				model.setBounds(20, 20, 80, 80);
				canvas.addShape(model);
				canvas.repaint();
			}
		});
		
		
		JButton setColor = new JButton("Set Color");
		setColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(canvas.selected()){
				Color color = JColorChooser.showDialog(Whiteboard.this, "Set Color", canvas.getSelected().getColor());
				canvas.recolorShape(color);	
				}
	            
			}
		});
		
		JButton delete = new JButton("Delete");
		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(canvas.selected()){
				canvas.removeShape();	
				repaint();
				}
			}
		});
		
		JButton toFront = new JButton("Move to Front");
		toFront.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvas.toFront();
	            repaint();
			}
		});
		
		JButton toBack = new JButton("Move to Back");
		toBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvas.toBack();
	            repaint();
			}
		});
		
		JButton setNull = new JButton("Clear");
		setNull.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvas.setNull();
	            repaint();
			}
		});
		
		JButton save = new JButton("Save");
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String result = JOptionPane.showInputDialog("File Name", null);
				if (result != null) {
					File f = new File(result);
					save(f);
				}
			}
		});
		
		JButton saveImage = new JButton("Save Image");
		saveImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String result = JOptionPane.showInputDialog("File Name", null);
				if (result != null) {
					File f = new File(result);
					saveImage(f);
				}
			}
		});
		
		JButton open = new JButton("Open");
		open.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String result = JOptionPane.showInputDialog("File Name", null);
				if (result != null) {
					File f = new File(result);
					open(f);
				}
			}
		});
		
		serverMode = new JButton("Server Mode"); 
		serverMode.addActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent e) { 
                startServer(); 
            } 
        }); 
         
		clientMode = new JButton("Client Mode"); 
		clientMode.addActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent e) { 
                startClient(); 
            } 
        }); 
		
		
		textField = new JTextField("Sample Text");
		textField.setMaximumSize(new Dimension(150, 20)); 
		textField.setPreferredSize(new Dimension(150, 20)); 
		textField.getDocument().addDocumentListener(new DocumentListener() { 
            public void changedUpdate(DocumentEvent e) { 
            } 
     
            public void insertUpdate(DocumentEvent e) { 
                handleTextChange(e); 
            } 
     
            public void removeUpdate(DocumentEvent e) { 
                handleTextChange(e); 
            } 
        }); 
		
		GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment(); 
        String fonts[] = g.getAvailableFontFamilyNames(); 
        fontMap = new HashMap<String, Integer>(); 
        for(int i = 0; i < fonts.length; i++) { 
            fontMap.put(fonts[i], i); 
        } 
		
		fontSelector = new JComboBox(fonts); 
        fontSelector.addActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent e) { 
                if(canvas.getSelected() instanceof DText) 
                    canvas.setFont((String)fontSelector.getSelectedItem()); 
            } 
        }); 
        
        fontSelector.setMaximumSize(new Dimension(100, 20)); 
        fontSelector.setPreferredSize(new Dimension(100, 20)); 
		
		tableModel = new TableModel();
		table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); 
        scrollpane = new JScrollPane(table); 
        scrollpane.setPreferredSize(new Dimension(380, 400));
		
		JPanel buttonPane = new JPanel();
		JPanel utilityPane = new JPanel();
		Box controlGroup = Box.createVerticalBox(); 
		Box addGroup = Box.createHorizontalBox();
		Box utilityGroup = Box.createHorizontalBox();
		Box functionGroup = Box.createHorizontalBox();
		Box tableGroup = Box.createHorizontalBox();
		Box networkingGroup = Box.createHorizontalBox();
		
		
		addGroup.add(addOval);
		addGroup.add(addRectangle);
		addGroup.add(addLine);
		addGroup.add(addText);
		utilityGroup.add(setNull);
		utilityGroup.add(delete);
		utilityGroup.add(toBack);
		utilityGroup.add(toFront);
		functionGroup.add(setColor);
		functionGroup.add(textField);
		functionGroup.add(fontSelector);
		networkingGroup.add(save);
		networkingGroup.add(saveImage);
		networkingGroup.add(open);
		networkingGroup.add(clientMode);
		networkingGroup.add(serverMode);
		tableGroup.add(scrollpane);
		controlGroup.add(addGroup);
		controlGroup.add(utilityGroup);
		controlGroup.add(functionGroup);
		controlGroup.add(networkingGroup);
		controlGroup.add(tableGroup);
		
		
		board.add(canvas);
        board.add(controlGroup, BorderLayout.WEST);
        board.setSize(1000, 500);
		board.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        board.setVisible(true);
    }
	
	private void handleTextChange(DocumentEvent e) { 
        if(canvas.selected() && canvas.getSelected() instanceof DText){
        	canvas.setText(textField.getText()); 
        }
            
    }
	
	public void updateTable(DShape selectedShape) { 
        table.clearSelection(); 
        if(selectedShape != null) { 
           int index = tableModel.getRow(selectedShape.getModel()); 
            table.setRowSelectionInterval(index, index); 
        } 
    }
	
	public void add(DShape shape) {
		tableModel.add(shape.getModel());
 
	} 
	
	public void delete(DShape shape) {
		tableModel.delete(shape.getModel());
		
	} 
	
	public void toBack(DShape shape) { 
	    tableModel.toBack(shape.getModel()); 
	   
	} 
	
	public void toFront(DShape shape) { 
	    tableModel.toFront(shape.getModel()); 
	} 
	
	public void clear() {
		tableModel.clear();
	} 

	public void save(File file){
		try {
			XMLEncoder xmlout =new XMLEncoder(new BufferedOutputStream(new FileOutputStream(file)));

			List<DShape> shapes = canvas.getShapes();
	    	DShapeModel[] models = new DShapeModel[shapes.size()];
	    	
	    	for(int i = 0; i < models.length; i ++){
	    		models[i] = shapes.get(i).getModel();
	    	}
			xmlout.writeObject(models);
			xmlout.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }
	
	public void saveImage(File file) {
		 
	     BufferedImage image = (BufferedImage) createImage(canvas.getWidth(), canvas.getHeight());
	     Graphics2D g2 = image.createGraphics();
	     canvas.paintAll(g2);
	    
	     try {
	         javax.imageio.ImageIO.write(image, "PNG", file);
	     }
	     catch (IOException ex) {
	         ex.printStackTrace();
	     }
		
	}
	
	public void open(File file){
		try {
			XMLDecoder xmlin = new XMLDecoder(new FileInputStream(file));
			DShapeModel[] models = (DShapeModel[]) xmlin.readObject();
			xmlin.close();
			
			
			clear();
			
			for(int i = 0; i < models.length; i++){
				canvas.addShape(models[i]);
			}
				
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
			
		}    	
    }
	
	public boolean notClient() { 
        return mode != client; 
    }
	
	public int getMode(){
		return mode;
	}
	
	public void startServer() { 
	    String result = JOptionPane.showInputDialog("Port", "25525"); 
	        
	    
	   if(result != null) { 
	    	clientMode.setEnabled(false); 
	        serverMode.setEnabled(false);
	        mode = server;

	   } 
	        
	} 

	    
	public void startClient() { 
	   String result = JOptionPane.showInputDialog("Connect to host", "127.0.0.1:25525");  
	   if(result != null) { 
	       String[] parts = result.split(":"); 
	       clientMode.setEnabled(false); 
	       serverMode.setEnabled(false);
	       mode = client; 
	       serverStarter = new ServerStarter();
	       //ServerStarter.start();
	            
	   }
	} 
	
	private class ServerStarter extends Thread{
		
		public void run(){
			
		}
		
	}
	
	public static void main(String args[]) {
		
		Whiteboard b = new Whiteboard(); 
	

	}

}
