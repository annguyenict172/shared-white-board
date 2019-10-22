package whiteboard;

import java.io.*;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class DrawingBoard extends Application{
	private  boolean status = false;		//if status = false, the picture have not been saved yet. if status = true, the picture have been saved
	private  String saveRoute;			//record the save route
	private  int eraserSize = 20;	//record the eraser size
	private  int fontSize = 20;		//record the text size
	private  int  lineWidth = 1;	//record the line width
	private  static Paint color = Color.BLACK;		//record the present color 
	private  int count = 0;			//used to control the sequence of text tool events
	private  DrawingBoardService dbService;
	
	private Stage loginStage;
	private Stage drawingStage;
	private String notificationMessage;
	
	// Main drawing board
	private AnchorPane root = new AnchorPane();
	private AnchorPane aPane = new AnchorPane();				//the canvas is on the aPane
	private Canvas canvas;			// the canvas, all the shapes are drown on it
	private GraphicsContext graph;	//the pen used to draw
	private AnchorPane textPane = new AnchorPane();				//used to show the text label and shape moving tracks
	private TextArea memberWindow = new TextArea();				//show the member list and some other information
	private TextField kickWindow = new TextField();				//kick window, input the name of the member that should be kicked out
	private	 TextArea communicationWindow = new TextArea();		//dialogue window
	private TextArea inputWindow = new TextArea();				//input window
	private Slider lineWidthSlider;
	
	// Login screen
	private Label loginStatusLabel = new Label();

	public static void main(String[] args) {
		launch(args);
	}
	
	// Initial stage with login modal
	public void start(Stage primaryStage) throws Exception {
		List<String> args = getParameters().getRaw();
		String hostName = args.get(0);
		int port = Integer.parseInt(args.get(1));
		
		dbService = new DrawingBoardService(hostName, port, "WhiteBoard", this);
		
		VBox vBox = new VBox();
		HBox hBox = new HBox();
		Label usernameLabel = new Label("Username");
		Label drawingidLabel = new Label("Drawing ID");
		TextField usernameField = new TextField();
		TextField drawingidField = new TextField();
		Button createButton = new Button("Create");
		Button joinButton = new Button("Join");
		Stage stage = new Stage();
		Scene scene = new Scene(vBox);
		
		createButton.setOnMouseClicked(new EventHandler<MouseEvent>() {		//create the board

			public void handle(MouseEvent event) {
				try {
					if(!usernameField.getText().equals("")) {  		//if username has been input
						String username = usernameField.getText();		//This is the username.the name should be delivered to other modules!!
						dbService.username = username;
						dbService.createDrawing();
						board();
						stage.close();
					}
					else {
						loginStatusLabel.setText("Please input an username");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		joinButton.setOnMouseClicked(new EventHandler<MouseEvent>() {		//join a board

			public void handle(MouseEvent event) {
				try {
					if(!usernameField.getText().equals("") && !drawingidField.getText().equals("")) {
						String username = usernameField.getText();		//This is the username.the name should be delivered to other modules!!
						String drawingid = drawingidField.getText();		//This is the drawingid.the id should be delivered to other modules!!
						loginStatusLabel.setText("Waiting for the acceptance......");
						//the codes here should check the drawingid 
						dbService.username = username;
						dbService.joinDrawing(drawingid);
//						stage.close();
					}
					else {
						loginStatusLabel.setText("Please input the username and drawing ID. ");
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		
		
		usernameField.setPrefWidth(200);
		usernameField.setPromptText("Please input username here.");
		usernameField.setFocusTraversable(false);
		drawingidField.setPrefWidth(200);
		drawingidField.setPromptText("Please input drawing id here.");
		drawingidField.setFocusTraversable(false);
		createButton.setPrefWidth(100);
		joinButton.setPrefWidth(100);
		hBox.setSpacing(120);
		vBox.setSpacing(15.0);
		vBox.setPadding(new Insets(30));
		hBox.getChildren().addAll(createButton, joinButton);
		vBox.getChildren().addAll(usernameLabel, usernameField, drawingidLabel, drawingidField, hBox, loginStatusLabel);
		stage.setScene(scene);
		stage.setHeight(350);
		stage.setWidth(400);
		stage.setTitle("IG Drawing Board login");
		loginStage = stage;
		stage.show();
//		stage.getIcons().add(new Image(getClass().getResourceAsStream("download.jpg")));		//set the icon of this app
	}
	
	// This method is used by the DrawingBoardService
	// so it can notify the DrawingBoard when it receives the response from server
	public void notify(String tag, Object data, String src) {
		System.out.println(tag + " " + data + " " + src);
		if (tag.compareTo(MessageTag.CHAT) == 0) {
			communicationWindow.appendText(src + ": " + (String) data + "\r\n");
		} else if (tag.compareTo(MessageTag.ASK_TO_JOIN) == 0) {
			Platform.runLater(new Runnable() {
                @Override public void run() {
                	boolean answer = ConfirmationBox.display("Confirmation", "Add user " + src + " to current drawing?");
                	if (answer) {
                		dbService.addToDrawing(src, (RMIDrawingClient) data); 
                	} else {
                		dbService.declineFromDrawing((RMIDrawingClient) data); 
                	}
                }
            });
        	
		} else if (tag.compareTo(MessageTag.MANAGER_APPROVED) == 0) {
			Platform.runLater(new Runnable() {
                 @Override public void run() {
                	 try {
                		 board();
                	 } catch (Exception e) {
         				System.err.println("Cannot open drawing application");
        				e.printStackTrace();
        			 }
                 }
             });
		} else if (tag.compareTo(MessageTag.MANAGER_DECLINED) == 0) {
			Platform.runLater(new Runnable() {
                @Override public void run() {
                	loginStatusLabel.setText("The manager has declined your request to join.");
                }
            });
		} else if (tag.compareTo(MessageTag.USERNAME_EXISTED) == 0) {
			Platform.runLater(new Runnable() {
                @Override public void run() {
                	loginStatusLabel.setText((String) data);
                }
            });
		} else if (tag.compareTo(MessageTag.NEW_MEMBER) == 0 || tag.compareTo(MessageTag.REMOVE_MEMBER) == 0 || tag.compareTo(MessageTag.MEMBER_QUIT) == 0) {
			memberWindow.clear();
			Vector<String> members = dbService.getMembers();
			if (members != null) {
				for (String member: members) {
					memberWindow.appendText(member + "\r\n");
				}
			}
			
			if (tag.compareTo(MessageTag.NEW_MEMBER) == 0) {
				notificationMessage = (String) data + " has joined the drawing";
			} else if (tag.compareTo(MessageTag.REMOVE_MEMBER) == 0) {
				notificationMessage = (String) data + " has been kicked out of the drawing";
			} else if (tag.compareTo(MessageTag.MEMBER_QUIT) == 0) {
				notificationMessage = (String) data + " has quit the drawing";
			}
			Platform.runLater(new Runnable() {
                @Override public void run() {
                	AlertBox.display("Notification", notificationMessage);
                }
            });
		} else if (tag.compareTo(MessageTag.MANAGER_QUIT) == 0) {
			Platform.runLater(new Runnable() {
                @Override public void run() {
                	AlertBox.display("Notification", "The manager has quit");
                	drawingStage.close();
                	loginStatusLabel.setText("");
                }
            });
		} else if (tag.compareTo(MessageTag.KICK_BY_MANAGER) == 0) {
			Platform.runLater(new Runnable() {
                @Override public void run() {
                	AlertBox.display("Notification", "You have been kicked by the manager");
                	drawingStage.close();
                	loginStatusLabel.setText("");
                }
            });
		} else if (tag.compareTo(MessageTag.DRAW) == 0) {
			if (src.compareTo(dbService.username) == 0) {
				return;
			}
			
			Platform.runLater(new Runnable() {
                @Override public void run() {
                	Hashtable<String, Object> instruction = (Hashtable<String, Object>) data;
                	drawWithInstruction(instruction);
                }
            });
		} else if (tag.compareTo(MessageTag.CURRENT_CHATS) == 0) {
			Platform.runLater(new Runnable() {
                @Override public void run() {
                	Vector<String> chats = (Vector<String>) data;
                	for (String chat: chats) {
                		communicationWindow.appendText(chat + "\r\n");
                	}
                }
            });
		} else if (tag.compareTo(MessageTag.CURRENT_DRAWING_INSTRUCTIONS) == 0) {
			Platform.runLater(new Runnable() {
                @Override public void run() {
                	Vector<Hashtable<String, Object>> instructions = (Vector<Hashtable<String, Object>>) data;
                	for (Hashtable<String, Object> instruction: instructions) {
                		drawWithInstruction(instruction);
                	}
                }
            });
		} else if (tag.compareTo(MessageTag.NEW_FILE) == 0) {
			Platform.runLater(new Runnable() {
                @Override public void run() {        			
        			SerializableImage serializeImage = (SerializableImage) data;
        			Image image = serializeImage.getImage();
        			graph.clearRect(0, 0, 10000, 10000);
        			graph.drawImage(image, 0, 0);
                }
            });
			
		}
	}
	
	// This function draw thing with an instruction
	public void drawWithInstruction(Hashtable<String, Object> instruction) {
		String drawType = (String) instruction.get("type");
		if (drawType.compareTo("line") == 0) {
        	Paint drawColor = Color.valueOf((String) instruction.get("color"));
        	double drawSize = (double) instruction.get("size");
        	
        	// Update
        	graph.setStroke(drawColor);
        	graph.setLineWidth((int) drawSize);
    		double x1 = (double) instruction.get("x1");
        	double y1 = (double) instruction.get("y1");
        	double x2 = (double) instruction.get("x2");
        	double y2 = (double) instruction.get("y2");
        	graph.strokeLine(x1, y1, x2, y2);
        	
    	} else if (drawType.compareTo("rect") == 0) {
    		Paint drawColor = Color.valueOf((String) instruction.get("color"));
        	double drawSize = (double) instruction.get("size");
        	
        	// Update
        	graph.setStroke(drawColor);
        	graph.setLineWidth((int) drawSize);
        	
    		double w = (double) instruction.get("w");
        	double h = (double) instruction.get("h");
        	double x = (double) instruction.get("x");
        	double y = (double) instruction.get("y");
        	graph.strokeRect(x, y, w, h);
    	} else if (drawType.compareTo("oval") == 0) {
    		Paint drawColor = Color.valueOf((String) instruction.get("color"));
        	double drawSize = (double) instruction.get("size");
        	
        	// Update
        	graph.setStroke(drawColor);
        	graph.setLineWidth((int) drawSize);
        	
    		double w = (double) instruction.get("w");
        	double h = (double) instruction.get("h");
        	double x = (double) instruction.get("x");
        	double y = (double) instruction.get("y");
        	graph.strokeOval(x, y, w, h);
    	} else if (drawType.compareTo("erase") == 0) {
    		int w = (int) instruction.get("w");
        	int h = (int) instruction.get("h");
        	double x = (double) instruction.get("x");
        	double y = (double) instruction.get("y");
        	graph.clearRect(x, y, w, h);
    	} else if (drawType.compareTo("text") == 0) {
    		Paint drawColor = Color.valueOf((String) instruction.get("color"));
    		int tempFontSize = (int) instruction.get("fontSize");
        	String text = (String) instruction.get("text");
        	double x = (double) instruction.get("x");
        	double y = (double) instruction.get("y");
    		
        	// Update
    		graph.setFont(Font.font(tempFontSize));
			graph.setFill(drawColor);
			graph.fillText(text, x, y);
    	}
    	
    	// Reset
    	graph.setFont(Font.font(fontSize));
		graph.setFill(color);
    	graph.setStroke(color);
    	graph.setLineWidth((int) lineWidthSlider.getValue());
	}
	
	
	// This method is used by the DrawingBoardService
	// so it can notify the DrawingBoard when it receives any errors from server
	public void notifyError(String error) {
		Platform.runLater(new Runnable() {
            @Override public void run() {
            	AlertBox.display("Error", error);
            }
        });
	}
	
	//  the drawning board gui
	public void board() throws Exception {
		System.out.println("Is manager: " + dbService.isManager());
		if (dbService.isManager()) {
			memberWindow.clear();
			Vector<String> members = dbService.getMembers();
			System.out.println("Initial members: " + members);
			if (members != null) {
				for (String member: members) {
					memberWindow.appendText(member + "\r\n");
				}
			}
		}
		AnchorPane root = new AnchorPane();
		AnchorPane aPane = new AnchorPane();				//the canvas is on the aPane
		AnchorPane textPane = new AnchorPane();				//used to show the text label and shape moving tracks
		canvas = new Canvas(1245, 775);
		graph = canvas.getGraphicsContext2D();
		graph.setLineCap(StrokeLineCap.ROUND);
//		used to set the width of line 
		lineWidthSlider = new Slider(1, 30, 1);	
		Label lineWidthShowing = new Label();
		lineWidthShowing.setText(Double.toString(1.0));
		lineWidthSlider.setOnMousePressed(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				lineWidthSlider.setOnMouseReleased(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent event) {
						lineWidthShowing.setText(Double.toString((int)lineWidthSlider.getValue()));
						graph.setLineWidth((int)lineWidthSlider.getValue());
						lineWidth = (int)lineWidthSlider.getValue();
					}
				});
			}
		});
//		used to set the size of eraser	
		Slider eraserWidthSlider = new Slider(20, 200, 20);
		Label eraserWidthShowing = new Label();
		eraserWidthShowing.setText(Double.toString(20.0));
		eraserWidthSlider.setOnMousePressed(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				eraserWidthSlider.setOnMouseReleased(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent event) {
						eraserWidthShowing.setText(Double.toString((int)eraserWidthSlider.getValue()));
						eraserSize = (int)eraserWidthSlider.getValue();
					}
				});
			}
		});
//		used to set the size of text	
		Slider textSizeSlider = new Slider(10, 60, 20);
		Label textSizeShowing = new Label();
		textSizeShowing.setText(Double.toString(20.0));
		textSizeShowing.setPrefWidth(50);
		textSizeSlider.setOnMousePressed(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				textSizeSlider.setOnMouseReleased(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent event) {
						textSizeShowing.setText(Double.toString((int)textSizeSlider.getValue()));
						fontSize = (int)textSizeSlider.getValue();
					}
				});
			}
		});
		
//		sliders of line width and eraser size
		VBox slider = new VBox();
		HBox lineSliderBox = new HBox();
		HBox eraserSliderBox = new HBox();
		HBox textSliderBox = new HBox();
		Label l1 = new Label("Line");
		l1.setPrefWidth(60);
		Label l2 = new Label("Eraser");
		l2.setPrefWidth(60);
		Label l3 = new Label("Text");
		l3.setPrefWidth(60);
		
		lineSliderBox.getChildren().addAll(l1, lineWidthSlider, lineWidthShowing);
		eraserSliderBox.getChildren().addAll(l2, eraserWidthSlider, eraserWidthShowing);
		textSliderBox.getChildren().addAll(l3, textSizeSlider, textSizeShowing);
		slider.setPadding(new Insets(5.0));
		slider.setSpacing(10.0);
		slider.getChildren().addAll(lineSliderBox, eraserSliderBox, textSliderBox);
		
//		menu part
		MenuBar menuBar = new MenuBar();
		Menu fileMenu = new Menu("File");
		Menu helpMenu = new Menu("Help");
		MenuItem item1 = new MenuItem("New");
		MenuItem item2 = new MenuItem("Open");
		MenuItem item3 = new MenuItem("Save");
		MenuItem item4 = new MenuItem("Save As");
		MenuItem item5 = new MenuItem("Close");
		MenuItem item6 = new MenuItem("Guide");
		fileMenu.getItems().addAll(item1, item2, item3, item4, item5);
		helpMenu.getItems().addAll(item6);
		if (dbService.isManager()) {
			menuBar.getMenus().addAll(fileMenu, helpMenu);
		}
		else {
			menuBar.getMenus().addAll(helpMenu);
		}

//		all menu items events
		item1.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				try {
					newFile(canvas, graph);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		item2.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				try {
					open(graph);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		item3.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				try {
					save(canvas, graph, 0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		item4.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				try {
					saveAs(canvas, graph,  0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		item5.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				try {
					close(canvas, graph);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		item6.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				try {
					guide();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		
//		random line part (message transfer)!!!
		Tooltip tip1 = new Tooltip("Random line tool");
		tip1.setFont(Font.font(15));	
		Button randomLineButton = new Button("", new ImageView(new Image(getClass().getResourceAsStream("randomLine.png"))));
		randomLineButton.setTooltip(tip1);
		randomLineButton.setPrefHeight(55);
		randomLineButton.setPrefWidth(55);
		randomLineButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			double x;
			double y;
			public void handle(MouseEvent event) {
				aPane.setOnDragDetected(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent event) {
						aPane.startFullDrag();						//must have this method, if you want to have full drag operation
						x = event.getX();
						y = event.getY();
					}
				});
				aPane.setOnMouseDragOver(new EventHandler<MouseDragEvent>() {		
					public void handle(MouseDragEvent event) {
						if(x==-1 && y==-1) {
							x = event.getX();
							y = event.getY();
						}
						graph.strokeLine(x, y, event.getX(), event.getY());
						
						// Boardcast line drawing
						Hashtable<String, Object> instruction = new Hashtable<String, Object>();
						
						instruction.put("type", "line");
						instruction.put("color", color.toString());
						instruction.put("size", lineWidthSlider.getValue());
						instruction.put("x1", x);
						instruction.put("y1", y);
						instruction.put("x2", event.getX());
						instruction.put("y2", event.getY());
						dbService.broadcast(MessageTag.DRAW, instruction);
						
						x = event.getX();
						y = event.getY();
					}
				});
				aPane.setOnMouseDragReleased(new EventHandler<MouseDragEvent>() {		
					public void handle(MouseDragEvent event) {
						x = -1;
						y = -1;
					}
				});	
			}
		});
		
//		Line part(message transfer)!!!
		Tooltip tip2 = new Tooltip("Line tool");
		tip2.setFont(Font.font(15));		
		Button lineButton = new Button("", new ImageView(new Image(getClass().getResourceAsStream("straightLine.png"))));
		lineButton.setPrefHeight(55);
		lineButton.setPrefWidth(55);
		lineButton.setTooltip(tip2);
		lineButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			double x1;
			double y1;
			double x2;
			double y2;
			public void handle(MouseEvent event) {
				aPane.setOnDragDetected(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent event) {
						aPane.startFullDrag();						//must have this method, if you want to have full drag operation
						x1 = event.getX();
						y1 = event.getY();
					}
				});
				aPane.setOnMouseDragOver(new EventHandler<MouseDragEvent>() {		//we must have this method, although it do nothing
					public void handle(MouseDragEvent event) {
						textPane.getChildren().clear();
						x2 = event.getX();
						y2 = event.getY();
						Line lineTrack = new Line(x1, y1, x2, y2);
						lineTrack.setStrokeWidth(lineWidthSlider.getValue());
						lineTrack.setOpacity(0.05);
						textPane.getChildren().add(lineTrack);
					}
				});
				aPane.setOnMouseDragReleased(new EventHandler<MouseDragEvent>() {	
					public void handle(MouseDragEvent event) {
						textPane.getChildren().clear();
						x2 = event.getX();
						y2 = event.getY();
						graph.strokeLine(x1, y1, x2, y2);
						
						// Boardcast line drawing
						Hashtable<String, Object> instruction = new Hashtable<String, Object>();
						
						instruction.put("type", "line");
						instruction.put("color", color.toString());
						instruction.put("size", lineWidthSlider.getValue());
						instruction.put("x1", x1);
						instruction.put("y1", y1);
						instruction.put("x2", x2);
						instruction.put("y2", y2);
						dbService.broadcast(MessageTag.DRAW, instruction);
					}
				});
			}
		});
		
// 		rec tool(message transfer)!!!
		Tooltip tip3 = new Tooltip("Rectangle tool");
		tip3.setFont(Font.font(15));	
		Button rectangleButton = new Button("", new ImageView(new Image(getClass().getResourceAsStream("rectangle.png"))));
		rectangleButton.setTooltip(tip3);
		rectangleButton.setPrefHeight(55);
		rectangleButton.setPrefWidth(55);
		rectangleButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			double x;
			double y;
			double w;
			double h;
			public void handle(MouseEvent event) {
				aPane.setOnDragDetected(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent event) {
						aPane.startFullDrag();						//must have this method, if you want to have full drag operation
						x = event.getX();
						y = event.getY();
					}
				});
				aPane.setOnMouseDragOver(new EventHandler<MouseDragEvent>() {		//we must have this method, although it do nothing
					public void handle(MouseDragEvent event) {
						textPane.getChildren().clear();
						w = event.getX() - x;
						h = event.getY() - y;
						if(w>0 && h>0) {
							Rectangle recTrack = new Rectangle(x, y, w, h);
							recTrack.setOpacity(0.03);
							textPane.getChildren().add(recTrack);
						}
						else if(w>0 && h<0) {
							Rectangle recTrack = new Rectangle(x, y+h, w, -h);
							recTrack.setOpacity(0.03);
							textPane.getChildren().add(recTrack);
						}
						else if(w<0 && h>0) {
							Rectangle recTrack = new Rectangle(x+w, y, -w, h);
							recTrack.setOpacity(0.03);
							textPane.getChildren().add(recTrack);
						}
						else if(w<0 && h<0) {
							Rectangle recTrack = new Rectangle(x+w, y+h, -w, -h);
							recTrack.setOpacity(0.03);
							textPane.getChildren().add(recTrack);
						}
					}
				});
				aPane.setOnMouseDragReleased(new EventHandler<MouseDragEvent>() {	
					public void handle(MouseDragEvent event) {
						double W;
						double H;
						double X;
						double Y;
						textPane.getChildren().clear();
						w = event.getX() - x;
						h = event.getY() - y;
						if(w>0 && h>0) {
							X = x;
							Y = y;
							W = w;
							H = h;
						} else if (w>0 && h<0) {
							X = x;
							Y = y + h;
							W = w;
							H = -h;
						} else if (w<0 && h>0) {
							X = x + w;
							Y = y;
							W = -w;
							H = h;
						} else {
							X = x + w;
							Y = y + h;
							W = -w;
							H = -h;
						}
						graph.strokeRect(X, Y, W, H);
						
						// Boardcast rect drawing
						Hashtable<String, Object> instruction = new Hashtable<String, Object>();
						
						instruction.put("type", "rect");
						instruction.put("color", color.toString());
						instruction.put("size", lineWidthSlider.getValue());
						instruction.put("x", X);
						instruction.put("y", Y);
						instruction.put("w", W);
						instruction.put("h", H);
						dbService.broadcast(MessageTag.DRAW, instruction);
					}
				});
			}
		});
		
		
// 		oval tool(message transfer)!!!
		Tooltip tip4 = new Tooltip("Oval tool. Draw circle when pressing Ctrl.");
		tip4.setFont(Font.font(15));	
		Button roundButton = new Button("", new ImageView(new Image(getClass().getResourceAsStream("oval.png"))));
		roundButton.setTooltip(tip4);
		roundButton.setPrefHeight(55);
		roundButton.setPrefWidth(55);
		roundButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			double x;
			double y;
			double w;
			double h;
			public void handle(MouseEvent event) {
				aPane.setOnDragDetected(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent event) {
						aPane.startFullDrag();						//must have this method, if you want to have full drag operation
						x = event.getX();
						y = event.getY();
					}
				});
				aPane.setOnMouseDragOver(new EventHandler<MouseDragEvent>() {		//we must have this method, although it do nothing
					public void handle(MouseDragEvent event) {
						textPane.getChildren().clear();
						w = (event.getX()-x);
						h = (event.getY()-y);
						if(!event.isControlDown()) {				//if do not press ctrl, draw oval
							if(w>0 && h>0) {
								Ellipse ellipseTrack = new Ellipse(x+w/2, y+h/2, w/2, h/2);
								ellipseTrack.setOpacity(0.03);
								textPane.getChildren().add(ellipseTrack);
							}
							else if(w>0 && h<0) {
								Ellipse ellipseTrack = new Ellipse(x+w/2, y+h/2, w/2, -h/2);
								ellipseTrack.setOpacity(0.03);
								textPane.getChildren().add(ellipseTrack);
							}
							else if(w<0 && h>0) {
								Ellipse ellipseTrack = new Ellipse(x+w/2, y+h/2, -w/2, h/2);
								ellipseTrack.setOpacity(0.03);
								textPane.getChildren().add(ellipseTrack);
							}
							else if(w<0 && h<0) {
								Ellipse ellipseTrack = new Ellipse(x+w/2, y+h/2, -w/2, -h/2);
								ellipseTrack.setOpacity(0.03);
								textPane.getChildren().add(ellipseTrack);
							}
						}
						else if(event.isControlDown()) {			//draw circle when press ctrl
							if((Math.abs(w)-Math.abs(h))>0) {
								if(w>0 && h>0) {
									Circle circleTrack = new Circle(x+h/2, y+h/2, h/2);
									circleTrack.setOpacity(0.03);
									textPane.getChildren().add(circleTrack);
								}
								else if(w>0 && h<0) {
									Circle circleTrack = new Circle(x-h/2, y+h/2, -h/2);
									circleTrack.setOpacity(0.03);
									textPane.getChildren().add(circleTrack);
								}
								else if(w<0 && h>0) {
									Circle circleTrack = new Circle(x-h/2, y+h/2, h/2);
									circleTrack.setOpacity(0.03);
									textPane.getChildren().add(circleTrack);
								}
								else if(w<0 && h<0) {
									Circle circleTrack = new Circle(x+h/2, y+h/2, -h/2);
									circleTrack.setOpacity(0.03);
									textPane.getChildren().add(circleTrack);
								}
							}
							else if((Math.abs(w)-Math.abs(h))<0) {
								if(w>0 && h>0) {
									Circle circleTrack = new Circle(x+w/2, y+w/2, w/2);
									circleTrack.setOpacity(0.03);
									textPane.getChildren().add(circleTrack);
								}
								else if(w>0 && h<0) {
									Circle circleTrack = new Circle(x+w/2, y-w/2, w/2);
									circleTrack.setOpacity(0.03);
									textPane.getChildren().add(circleTrack);
								}
								else if(w<0 && h>0) {
									Circle circleTrack = new Circle(x+w/2, y-w/2, -w/2);
									circleTrack.setOpacity(0.03);
									textPane.getChildren().add(circleTrack);
								}
								else if(w<0 && h<0) {
									Circle circleTrack = new Circle(x+w/2, y+w/2, -w/2);
									circleTrack.setOpacity(0.03);
									textPane.getChildren().add(circleTrack);
								}
							}
						}
					}
				});
				aPane.setOnMouseDragReleased(new EventHandler<MouseDragEvent>() {	
					public void handle(MouseDragEvent event) {
						double X;
						double Y;
						double W;
						double H;
						textPane.getChildren().clear();
						w = (event.getX()-x);
						h = (event.getY()-y);
						if(!event.isControlDown()) {				//if do not press ctrl, draw oval
							if(w>0 && h>0) {
								X = x;
								Y = y;
								W = w;
								H = h;
							} else if(w>0 && h<0) {
								X = x;
								Y = y + h;
								W = w;
								H = -h;
							} else if(w<0 && h>0) {
								X = x + w;
								Y = y;
								W = -w;
								H = h;
							} else {
								X = x + w;
								Y = y + h;
								W = -w;
								H = -h;
							}
						}
						else {			//draw circle when press ctrl
							if((Math.abs(w)-Math.abs(h))>0) {
								if(w>0 && h>0) {
									X = x;
									Y = y;
									W = h;
									H = h;
								} else if(w>0 && h<0) {
									X = x;
									Y = y + h;
									W = -h;
									H = -h;
								} else if(w<0 && h>0) {
									X = x - h;
									Y = y;
									W = h;
									H = h;
								} else {
									X = x + h;
									Y = y + h;
									W = -h;
									H = -h;
								}
							}
							else {
								if(w>0 && h>0) {
									X = x;
									Y = y;
									W = w;
									H = w;
								} else if(w>0 && h<0) {
									X = x;
									Y = y - w;
									W = w;
									H = w;
								} else if(w<0 && h>0) {
									X = x + w;
									Y = y;
									W = -w;
									H = -w;
								} else {
									X = x + w;
									Y = y + w;
									W = -w;
									H = -w;
								}
							}
						}
						
						graph.strokeOval(X, Y, W, H);
						
						// Boardcast oval drawing
						Hashtable<String, Object> instruction = new Hashtable<String, Object>();
						
						instruction.put("type", "oval");
						instruction.put("color", color.toString());
						instruction.put("size", lineWidthSlider.getValue());
						instruction.put("x", X);
						instruction.put("y", Y);
						instruction.put("w", W);
						instruction.put("h", H);
						dbService.broadcast(MessageTag.DRAW, instruction);
					}
				});
			}
		});	

// 		eraser tool(message transfer)!!!
		Tooltip tip5 = new Tooltip("Eraser tool");
		tip5.setFont(Font.font(15));	
		Button eraserButton = new Button("", new ImageView(new Image(getClass().getResourceAsStream("rubber.png"))));
		eraserButton.setTooltip(tip5);
		eraserButton.setPrefHeight(55);
		eraserButton.setPrefWidth(55);
		eraserButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			double x;
			double y;
			public void handle(MouseEvent event) {
				aPane.setOnDragDetected(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent event) {
						aPane.startFullDrag();						//must have this method, if you want to have full drag operation
						Rectangle eraserTrack = new Rectangle(event.getX()-eraserSize/2, event.getY()-eraserSize/2, eraserSize, eraserSize);
						eraserTrack.setOpacity(0.05);
						textPane.getChildren().add(eraserTrack);
					}
				});
				aPane.setOnMouseDragOver(new EventHandler<MouseDragEvent>() {		//we must have this method, although it do nothing
					public void handle(MouseDragEvent event) {
						textPane.getChildren().clear();
						x = event.getX();
						y = event.getY();
						Rectangle eraserTrack = new Rectangle(event.getX()-eraserSize/2, event.getY()-eraserSize/2, eraserSize, eraserSize);
						eraserTrack.setOpacity(0.05);
						textPane.getChildren().add(eraserTrack);
						graph.clearRect(x-eraserSize/2, y-eraserSize/2, eraserSize, eraserSize);
						
						// Boardcast eraser
						Hashtable<String, Object> instruction = new Hashtable<String, Object>();
						
						instruction.put("type", "erase");
						instruction.put("x", x-eraserSize/2);
						instruction.put("y", y-eraserSize/2);
						instruction.put("w", eraserSize);
						instruction.put("h", eraserSize);
						dbService.broadcast(MessageTag.DRAW, instruction);
					}
				});
				aPane.setOnMouseDragReleased(new EventHandler<MouseDragEvent>() {	
					public void handle(MouseDragEvent event) {
						textPane.getChildren().clear();
					}
				});
			}
		});
		
//		text tool(message transfer)!!!
		Tooltip tip6 = new Tooltip("Text tool");
		tip6.setFont(Font.font(15));	
		Button textButton = new Button("A");
		textButton.setFont(Font.font(26));
		textButton.setTooltip(tip6);
		textButton.setPrefHeight(55);
		textButton.setPrefWidth(55);
		textButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			double x;
			double y;
			TextField text;
			public void handle(MouseEvent event) {
				count = 0;
				textPane.getChildren().clear();
				root.setOnMouseClicked(new EventHandler<MouseEvent>() {					
						public void handle(MouseEvent event) {
							if(count == 0) {
								text = new TextField();
								x = event.getX()-5;
								y = event.getY()-125;
								text.setOpacity(0.5);
								text.setFont(Font.font(fontSize));;
								text.setPromptText("Input text here");
								text.setFocusTraversable(false);	
								textPane.getChildren().add(text);
								textPane.setLeftAnchor(text, x);
								textPane.setTopAnchor(text, y);
								count = 1;
							}
							else if(count == 1) {
								textPane.getChildren().removeAll(text);
								graph.setFont(Font.font(fontSize));
								graph.setFill(color);
								graph.fillText(text.getText(), x+fontSize*0.65, y+fontSize*1.33);
								count = 2;
								
								// Boardcast text
								Hashtable<String, Object> instruction = new Hashtable<String, Object>();
								
								instruction.put("type", "text");
								instruction.put("color", color.toString());
								instruction.put("fontSize", fontSize);
								instruction.put("text", text.getText());
								instruction.put("x", x+fontSize*0.65);
								instruction.put("y", y+fontSize*1.33);
								dbService.broadcast(MessageTag.DRAW, instruction);
							}							
					}
				});
			}
		});	
		
//		Color Palette
		Rectangle colorShowingRectangle = new Rectangle(30,30);
		colorShowingRectangle.setFill(Color.BLACK);
		VBox colorVBox = new VBox();
		colorVBox.setPadding(new Insets(20.0));
		colorVBox.getChildren().addAll(colorShowingRectangle);
		
		VBox colorVBox2 = new VBox(); 
		
		HBox colorPalettePane1 = new HBox();
		colorPalettePane1.setSpacing(2.0);
		colorPalettePane1.setPrefSize(180, 29);
		Rectangle color1 = new Rectangle(28, 28);
		color1.setFill(Color.valueOf("#FFFFFF"));
		color1.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				colorShowingRectangle.setFill(Color.valueOf("#FFFFFF"));
                graph.setStroke(Color.valueOf("#FFFFFF"));
                color = Color.valueOf("#FFFFFF");
			}
		});
		Rectangle color2 = new Rectangle(28, 28);
		color2.setFill(Color.valueOf("#FFFFE0"));
		color2.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				colorShowingRectangle.setFill(Color.valueOf("#FFFFE0"));
                graph.setStroke(Color.valueOf("#FFFFE0"));
                color = Color.valueOf("#FFFFE0");
			}
		});
		Rectangle color3 = new Rectangle(28, 28);
		color3.setFill(Color.valueOf("#FFFF00"));
		color3.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				colorShowingRectangle.setFill(Color.valueOf("#FFFF00"));
                graph.setStroke(Color.valueOf("#FFFF00"));
                color = Color.valueOf("#FFFF00");
			}
		});
		Rectangle color4 = new Rectangle(28, 28);
		color4.setFill(Color.valueOf("#FFA500"));
		color4.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				colorShowingRectangle.setFill(Color.valueOf("#FFA500"));
                graph.setStroke(Color.valueOf("#FFA500"));
                color = Color.valueOf("#FFA500");
			}
		});
		Rectangle color5 = new Rectangle(28, 28);
		color5.setFill(Color.valueOf("#FF69B4"));
		color5.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				colorShowingRectangle.setFill(Color.valueOf("#FF69B4"));
                graph.setStroke(Color.valueOf("#FF69B4"));
                color = Color.valueOf("#FF69B4");
			}
		});
		Rectangle color6 = new Rectangle(28, 28);
		color6.setFill(Color.valueOf("#FF0000"));
		color6.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				colorShowingRectangle.setFill(Color.valueOf("#FF0000"));
                graph.setStroke(Color.valueOf("#FF0000"));
                color = Color.valueOf("#FF0000");
			}
		});
		
		HBox colorPalettePane2 = new HBox();
		colorPalettePane2.setSpacing(2.0);
		colorPalettePane2.setPrefSize(180, 29);
		Rectangle color7 = new Rectangle(28, 28);
		color7.setFill(Color.valueOf("#BFEFFF"));
		color7.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				colorShowingRectangle.setFill(Color.valueOf("#BFEFFF"));
                graph.setStroke(Color.valueOf("#BFEFFF"));
                color = Color.valueOf("#BFEFFF");
			}
		});
		Rectangle color8 = new Rectangle(28, 28);
		color8.setFill(Color.valueOf("#4169E1"));
		color8.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				colorShowingRectangle.setFill(Color.valueOf("#4169E1"));
                graph.setStroke(Color.valueOf("#4169E1"));
                color = Color.valueOf("#4169E1");
			}
		});
		Rectangle color9 = new Rectangle(28, 28);
		color9.setFill(Color.valueOf("#9400D3"));
		color9.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				colorShowingRectangle.setFill(Color.valueOf("#9400D3"));
                graph.setStroke(Color.valueOf("#9400D3"));
                color = Color.valueOf("#9400D3");
			}
		});
		Rectangle color10 = new Rectangle(28, 28);
		color10.setFill(Color.valueOf("#191970"));
		color10.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				colorShowingRectangle.setFill(Color.valueOf("#191970"));
                graph.setStroke(Color.valueOf("#191970"));
                color = Color.valueOf("#191970");
			}
		});
		Rectangle color11 = new Rectangle(28, 28);
		color11.setFill(Color.valueOf("#8B1A1A"));
		color11.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				colorShowingRectangle.setFill(Color.valueOf("#8B1A1A"));
                graph.setStroke(Color.valueOf("#8B1A1A"));
                color = Color.valueOf("#8B1A1A");
			}
		});
		Rectangle color12 = new Rectangle(28, 28);
		color12.setFill(Color.valueOf("#000000"));
		color12.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				colorShowingRectangle.setFill(Color.valueOf("#000000"));
                graph.setStroke(Color.valueOf("#000000"));
                color = Color.valueOf("#000000");
			}
		});
		
		HBox colorPalettePane3 = new HBox();
		colorPalettePane3.setSpacing(2.0);
		colorPalettePane3.setPrefSize(190, 29);
		Rectangle color13 = new Rectangle(28, 28);
		color13.setFill(Color.valueOf("#7FFFD4"));
		color13.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				colorShowingRectangle.setFill(Color.valueOf("#7FFFD4"));
                graph.setStroke(Color.valueOf("#7FFFD4"));
                color = Color.valueOf("#7FFFD4");
			}
		});
		Rectangle color14 = new Rectangle(28, 28);
		color14.setFill(Color.valueOf("#7CFC00"));
		color14.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				colorShowingRectangle.setFill(Color.valueOf("#7CFC00"));
                graph.setStroke(Color.valueOf("#7CFC00"));
                color = Color.valueOf("#7CFC00");
			}
		});
		Rectangle color15 = new Rectangle(28, 28);
		color15.setFill(Color.valueOf("#6E8B3D"));
		color15.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				colorShowingRectangle.setFill(Color.valueOf("#6E8B3D"));
                graph.setStroke(Color.valueOf("#6E8B3D"));
                color = Color.valueOf("#6E8B3D");
			}
		});
		Rectangle color16 = new Rectangle(28, 28);
		color16.setFill(Color.valueOf("#388E8E"));
		color16.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				colorShowingRectangle.setFill(Color.valueOf("#388E8E"));
                graph.setStroke(Color.valueOf("#388E8E"));
                color = Color.valueOf("#388E8E");
			}
		});
		Rectangle color17 = new Rectangle(28, 28);
		color17.setFill(Color.valueOf("#006400"));
		color17.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				colorShowingRectangle.setFill(Color.valueOf("#006400"));
                graph.setStroke(Color.valueOf("#006400"));
                color = Color.valueOf("#006400");
			}
		});
		Rectangle color18 = new Rectangle(28, 28);
		color18.setFill(Color.valueOf("#00688B"));
		color18.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				colorShowingRectangle.setFill(Color.valueOf("#00688B"));
                graph.setStroke(Color.valueOf("#00688B"));
                color = Color.valueOf("#00688B");
			}
		});
			
		colorPalettePane1.getChildren().addAll(color1, color2, color3, color4, color5, color6);
		colorPalettePane2.getChildren().addAll(color7, color8, color9, color10, color11, color12);
		colorPalettePane3.getChildren().addAll(color13, color14, color15, color16, color17, color18);
		colorVBox2.getChildren().addAll(colorPalettePane1, colorPalettePane2, colorPalettePane3);				
		
//		Color Picker
		Tooltip tip7 = new Tooltip("Color Picker");
		tip7.setFont(Font.font(15));	
		Button ColorPickerButton = new Button("", new ImageView(new Image(getClass().getResourceAsStream("colorPicker.png"))));
		ColorPickerButton.setFont(Font.font(26));
		ColorPickerButton.setTooltip(tip7);
		ColorPickerButton.setPrefHeight(55);
		ColorPickerButton.setPrefWidth(55);
		ColorPickerButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			
			public void handle(MouseEvent event) {
				colorPicker(canvas, graph, colorShowingRectangle);
			}
		});
		
//		all the tools on this panel
		Line separateLine1 = new Line(0,0,0,80);
		separateLine1.setStroke(Color.valueOf("#C0C0C0"));
		Line separateLine2 = new Line(0,0,0,80);
		separateLine2.setStroke(Color.valueOf("#C0C0C0"));
		HBox toolBox = new HBox();
		toolBox.setPadding(new Insets(5.0));
		toolBox.setSpacing(5);					//space between tools
		toolBox.setAlignment(Pos.TOP_LEFT);		//all the tools are top-left
		toolBox.getChildren().addAll(randomLineButton, lineButton, rectangleButton, roundButton, eraserButton, textButton, separateLine1, slider, separateLine2, colorVBox, colorVBox2, ColorPickerButton);			//add all the tool buttons here
//		the label on the right		
		Label label1 = new Label("		    Members list");
		Label label2 = new Label("		       Dialogue");
		
//		the windows on the right
		communicationWindow.setWrapText(true);		//line break
		inputWindow.setPrefSize(240, 70);
		inputWindow.setPromptText("Input text here");
		inputWindow.setFocusTraversable(false);		
		communicationWindow.setPrefHeight(300);
		Button inputButton = new Button("<-");
		inputButton.setPrefSize(70, 70);
		HBox inputWinBox = new HBox();
		inputWinBox.setSpacing(5);
		inputWinBox.getChildren().addAll(inputWindow, inputButton);		//the input window and input button
		
// 		input part(message transfer)!!!
		inputButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				try {
					dbService.broadcast(MessageTag.CHAT, inputWindow.getText());
					inputWindow.clear();
				} catch (Exception e) {
					System.err.println("Error communicating with server: " + e);
				}
			}
		});

//		kickWindow
		kickWindow.setPrefSize(240, 70);
		kickWindow.setPromptText("Kick user");
		kickWindow.setFocusTraversable(false);	
		
//		kick button(message transfer)!!!
		Button kickButton = new Button("Kick");
		kickButton.setPrefSize(100, 30);		
		kickButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent arg0) {
				String memberKicked = kickWindow.getText();			//get the name of member that should be kicked out
				dbService.removeMember(memberKicked);
				kickWindow.clear();
				//the operation to kick the member should be added here
			}
		});
		
		
//		communication and input windows are on this panel
		VBox windowPane = new VBox();
		windowPane.setPrefWidth(280);
		windowPane.setStyle("-fx-background-color:#ADD8E6;");	
		if(dbService.isManager()) {
			windowPane.getChildren().addAll(label1, memberWindow, kickWindow, kickButton, label2, communicationWindow, inputWinBox);
		}
		else {
			windowPane.getChildren().addAll(label1, memberWindow, label2, communicationWindow, inputWinBox);
		}
		windowPane.setPadding(new Insets(5.0));
		windowPane.setSpacing(5.0);

		aPane.setStyle("-fx-background-color:#FFFFFF;");	//white color board	
		aPane.getChildren().addAll(canvas, textPane);					//children of aPane
		aPane.setTopAnchor(canvas, 0.0);			//the size of the aPane should be changed following the stage
		aPane.setLeftAnchor(canvas, 0.0);
		aPane.setRightAnchor(canvas, 0.0);
		aPane.setBottomAnchor(canvas, 0.0);
		
//	    root anchorPane
		root.setStyle("-fx-background-color:#F8F8FF;");	
		root.getChildren().addAll(aPane, toolBox, windowPane, menuBar);
		root.setTopAnchor(aPane, 125.0);			//the size of the aPane should be changed following the stage
		root.setLeftAnchor(aPane, 5.0);
		root.setRightAnchor(aPane, 285.0);
		root.setBottomAnchor(aPane, 5.0);
		root.setRightAnchor(windowPane, 0.0);
		root.setBottomAnchor(windowPane, 0.0);
		root.setTopAnchor(windowPane, 125.0);
		root.setTopAnchor(toolBox, 30.0);
		root.setLeftAnchor(toolBox, 50.0);
		root.setTopAnchor(menuBar, 0.0);
		
		Scene scene = new Scene(root);
		
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.setHeight(900);
		stage.setWidth(1300);
		stage.setMinHeight(500);
		stage.setMinWidth(1100);
		stage.setTitle("Drawing ID: " + dbService.drawingId);
		stage.show();
		stage.setOnCloseRequest(e -> {
			System.out.println("Close the app");
			dbService.quit();
			loginStatusLabel.setText("");
			stage.close();
		});
		
		drawingStage = stage;
		
		stage.getIcons().add(new Image(getClass().getResourceAsStream("download.jpg")));		//set the icon of this app

//		listen to the stage width and change the length of menubar
		menuBar.setPrefWidth(root.getWidth());
		root.widthProperty().addListener(new ChangeListener<Number>() {

			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				menuBar.setPrefWidth(newValue.doubleValue());
			}
			
		});
		
//		change the size of canvas according to the size of root panel
		aPane.widthProperty().addListener(new ChangeListener<Number>() {

			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				canvas.setWidth(newValue.doubleValue());
			}
			
		});
		aPane.heightProperty().addListener(new ChangeListener<Number>() {

			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				canvas.setHeight(newValue.doubleValue());
			}
			
		});
		
//		used to clear the track that does not disappear
		root.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				textPane.getChildren().clear();
			}
		});
	}	
	
//	the help -> guide stage
	public void guide() {
		Stage guideStage = new Stage();
		guideStage.setWidth(500);
		guideStage.setHeight(500);
		guideStage.show();
	}
	
	public void saveAs(Canvas canvas,GraphicsContext graph, int q) {			//if q =1 the system will be closed after saving
		AnchorPane aPane = new AnchorPane();
		
		Scene scene = new Scene(aPane);
		
		Stage Stage = new Stage();
		Stage.setTitle("Save As");
		Stage.setScene(scene);
		Stage.setWidth(600);
		Stage.setHeight(300);
		Stage.show();
		
		Button saveAsButton = new Button("Save as PNG");		
		
		TextField routeTextField = new TextField();
		routeTextField.setPrefSize(500, 40);
		routeTextField.setPromptText("Input the directory that you want to save the picture here.");
		routeTextField.setFocusTraversable(false);	
		
		TextField fileNameTextField = new TextField();
		fileNameTextField.setPrefSize(500, 40);		
		fileNameTextField.setPrefWidth(240);
		fileNameTextField.setPromptText("The name of the file here.");
		fileNameTextField.setFocusTraversable(false);	
		
		aPane.getChildren().addAll(fileNameTextField ,routeTextField, saveAsButton);
		aPane.setTopAnchor(saveAsButton, 200.0);
		aPane.setLeftAnchor(saveAsButton, 245.0);
		aPane.setTopAnchor(routeTextField, 100.0);
		aPane.setLeftAnchor(routeTextField, 40.0);
		aPane.setTopAnchor(fileNameTextField, 50.0);
		aPane.setLeftAnchor(fileNameTextField, 180.0);
		Label alertLabel = new Label();			//show the operation status 
		aPane.getChildren().add(alertLabel);
		
		saveAsButton.setOnMouseClicked(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				WritableImage image = canvas.snapshot(new SnapshotParameters(), null);
				try {
					File file = new File(routeTextField.getText() + "\\" + fileNameTextField.getText()+ ".png");
					ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
					alertLabel.setText("Saving success!");
					aPane.setTopAnchor(alertLabel, 150.0);
					aPane.setLeftAnchor(alertLabel, 245.0);
					saveRoute = routeTextField.getText() + "\\" + fileNameTextField.getText()+ ".png";
					status = true;
					if(q==1)
						System.exit(0);
					else if(q==2) {
						graph.clearRect(0, 0, 10000, 10000);
						status = false;
						Stage.close();
					}
				}catch(Exception e) {		
					alertLabel.setText("Sorry! Saving fails. Something wrong with the route or file name.");
					aPane.setTopAnchor(alertLabel, 150.0);
					aPane.setLeftAnchor(alertLabel, 50.0);
				}
			}
		});		
	} 
	
	public void newFile(Canvas canvas, GraphicsContext graph) {
		Button yesButton = new Button("YES");
		yesButton.setPrefWidth(100);
		Button noButton = new Button("NO");
		noButton.setPrefWidth(100);
		Label label = new Label("Do you want to save the current picture?");
		
		AnchorPane aPane = new AnchorPane();
		aPane.getChildren().addAll(yesButton, noButton, label);
		aPane.setTopAnchor(label, 100.0);
		aPane.setLeftAnchor(label, 150.0);
		aPane.setTopAnchor(yesButton, 150.0);
		aPane.setLeftAnchor(yesButton, 150.0);
		aPane.setTopAnchor(noButton, 150.0);
		aPane.setLeftAnchor(noButton, 350.0);
		
		Scene scene = new Scene(aPane);
		
		Stage stage = new Stage();
		stage.setTitle("Open");
		stage.setScene(scene);
		stage.setWidth(600);
		stage.setHeight(300);
		stage.show();
		
		noButton.setOnMouseClicked(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				graph.clearRect(0, 0, 10000, 10000);
				status = false;
				stage.close();
			}
		});
		yesButton.setOnMouseClicked(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				if(!status) {
					saveAs(canvas, graph, 2);		//2 means when finish the operation, the canvas will be cleared				
				} 
				else if(status) {
					save(canvas, graph, 2);
				}
				stage.close();
			}
		});
	}
	
	public void open(GraphicsContext graph) {
		Label alertLabel = new Label();			//show the operation status 
		
		AnchorPane aPane = new AnchorPane();
		
		Scene scene = new Scene(aPane);
		
		Stage stage = new Stage();
		stage.setTitle("Open");
		stage.setScene(scene);
		stage.setWidth(600);
		stage.setHeight(300);
		stage.show();
		
		Button open = new Button("Open");		
		
		TextField routeTextField = new TextField();
		routeTextField.setPrefSize(500, 40);
		routeTextField.setPromptText("Input the file route here.");
		routeTextField.setFocusTraversable(false);	
		
		aPane.getChildren().addAll(open ,routeTextField);
		aPane.setTopAnchor(open, 200.0);
		aPane.setLeftAnchor(open, 265.0);
		aPane.setTopAnchor(routeTextField, 80.0);
		aPane.setLeftAnchor(routeTextField, 40.0);
		aPane.getChildren().add(alertLabel);
		
		open.setOnMouseClicked(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				try {
					File file = new File(routeTextField.getText());
					if(file.exists()){				//to see whether the rought is right				
						Image image = new Image("file:"+routeTextField.getText());
						graph.clearRect(0, 0, 10000, 10000);
						graph.drawImage(image, 0, 0);
						
						// Broadcast the change
						SerializableImage serializeImage = new SerializableImage();
						serializeImage.setImage(image);
						dbService.broadcast(MessageTag.NEW_FILE, serializeImage);
						
						status = true;
						saveRoute = routeTextField.getText();
						stage.close();						
					}
					else {
						throw new Exception();
					}
				}catch(Exception e) {	
					alertLabel.setText("Fail! Something wrong with the route.");
					aPane.setTopAnchor(alertLabel, 150.0);
					aPane.setLeftAnchor(alertLabel, 180.0);
				}
			}
		});		
	}
	public void close(Canvas canvas, GraphicsContext graph) {
		Button yesButton = new Button("YES");
		yesButton.setPrefWidth(100);
		Button noButton = new Button("NO");
		noButton.setPrefWidth(100);
		Label label = new Label("Do you want to save the picture?");
		
		AnchorPane aPane = new AnchorPane();
		aPane.getChildren().addAll(yesButton, noButton, label);
		aPane.setTopAnchor(label, 100.0);
		aPane.setLeftAnchor(label, 180.0);
		aPane.setTopAnchor(yesButton, 150.0);
		aPane.setLeftAnchor(yesButton, 150.0);
		aPane.setTopAnchor(noButton, 150.0);
		aPane.setLeftAnchor(noButton, 350.0);
		
		Scene scene = new Scene(aPane);
		
		Stage stage = new Stage();
		stage.setTitle("Open");
		stage.setScene(scene);
		stage.setWidth(600);
		stage.setHeight(300);
		stage.show();
		
		noButton.setOnMouseClicked(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				System.exit(0);
			}
		});
		yesButton.setOnMouseClicked(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				if(!status) {
					saveAs(canvas, graph, 1);		//1 means when finish the operation, the system will be closed				
				}
				else if(status) {
					save(canvas, graph, 1);
				}
				stage.close();
			}
		});
	}
	public void save(Canvas canvas,GraphicsContext graph, int q) {
		if(!status)
			saveAs(canvas, graph, 0);
		else {
			try {
				File file = new File(saveRoute);
				WritableImage image = canvas.snapshot(new SnapshotParameters(), null);
				ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
				if(q==1)
					System.exit(0);
				else if(q==2) {
					graph.clearRect(0, 0, 10000, 10000);
					status = false;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void colorPicker(Canvas canvas, GraphicsContext graph, Rectangle rectangle) {
		Stage stage = new Stage();
		final ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue((Color) rectangle.getFill());
 
        final Circle circle = new Circle(50);
        circle.setFill(colorPicker.getValue());
 
        colorPicker.setOnAction(new EventHandler<ActionEvent>() {
 
            public void handle(ActionEvent event) {
                circle.setFill(colorPicker.getValue());
                rectangle.setFill(colorPicker.getValue());
                graph.setStroke(colorPicker.getValue());
                color = colorPicker.getValue();
            }
        });
 
        FlowPane root = new FlowPane();
        root.setPadding(new Insets(10));
        root.setHgap(10);
        root.getChildren().addAll(circle, colorPicker);
 
        Scene scene = new Scene(root, 400, 300);
 
        stage.setTitle("Customize the Color");
        stage.setScene(scene);
        stage.show();
	}
}
