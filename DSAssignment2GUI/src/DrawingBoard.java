import java.io.*;
import javax.imageio.ImageIO;
import javafx.application.Application;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class DrawingBoard extends Application{
	public static boolean status = false;		//if status = false, the picture have not been saved yet. if status = true, the picture have been saved
	public static String saveRoute;			//record the save route
	public static double eraserSize = 20.0;	//record the eraser size
	public static double fontSize = 20;		//record the text size
	public static int count = 0;			//used to control the sequence of text tool events

	public static void main(String[] args) {
		launch(args);
	}

	public void start(Stage primaryStage) throws Exception {
		AnchorPane root = new AnchorPane();
		AnchorPane aPane = new AnchorPane();				//the canvas is on the aPane
		AnchorPane textPane = new AnchorPane();				//used to show the text label and shape moving tracks
		Canvas canvas = new Canvas(1245, 775);
		GraphicsContext graph = canvas.getGraphicsContext2D();
		
//		used to set the width of line 
		Slider lineWidthSlider = new Slider(1, 30, 1);	
		Label lineWidthShowing = new Label();
		lineWidthShowing.setText(Double.toString(1.0));
		lineWidthSlider.setOnMousePressed(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				lineWidthSlider.setOnMouseReleased(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent event) {
						lineWidthShowing.setText(Double.toString((int)lineWidthSlider.getValue()));
						graph.setLineWidth((int)lineWidthSlider.getValue());
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
		menuBar.getMenus().addAll(fileMenu, helpMenu);

//		all menu items events
		item1.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				try {
					graph.clearRect(0, 0, 10000, 10000);
					status = false;
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
					save(canvas);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		item4.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				try {
					saveAs(canvas, 0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		item5.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				try {
					close(canvas);
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
		
		
//		random line part
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
		
//		Line part
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
					}
				});
			}
		});
		
// 		rec tool
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
						textPane.getChildren().clear();
						w = event.getX() - x;
						h = event.getY() - y;
						if(w>0 && h>0)
							graph.strokeRect(x, y, w, h);
						else if(w>0 && h<0)
							graph.strokeRect(x, y+h, w, -h);
						else if(w<0 && h>0)
							graph.strokeRect(x+w, y, -w, h);
						else if(w<0 && h<0)
							graph.strokeRect(x+w, y+h, -w, -h);
					}
				});
			}
		});
		
		
// 		oval tool
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
						textPane.getChildren().clear();
						w = (event.getX()-x);
						h = (event.getY()-y);
						if(!event.isControlDown()) {				//if do not press ctrl, draw oval
							if(w>0 && h>0)
								graph.strokeOval(x, y, w, h);
							else if(w>0 && h<0)
								graph.strokeOval(x, y+h, w, -h);
							else if(w<0 && h>0)
								graph.strokeOval(x+w, y, -w, h);
							else if(w<0 && h<0)
								graph.strokeOval(x+w, y+h, -w, -h);
						}
						else if(event.isControlDown()) {			//draw circle when press ctrl
							if((Math.abs(w)-Math.abs(h))>0) {
								if(w>0 && h>0)
									graph.strokeOval(x, y, h, h);
								else if(w>0 && h<0)
									graph.strokeOval(x, y+h, -h, -h);
								else if(w<0 && h>0)
									graph.strokeOval(x-h, y, h, h);
								else if(w<0 && h<0)
									graph.strokeOval(x+h, y+h, -h, -h);
							}
							else if((Math.abs(w)-Math.abs(h))<0) {
								if(w>0 && h>0)
									graph.strokeOval(x, y, w, w);
								else if(w>0 && h<0)
									graph.strokeOval(x, y-w, w, w);
								else if(w<0 && h>0)
									graph.strokeOval(x+w, y, -w, -w);
								else if(w<0 && h<0)
									graph.strokeOval(x+w, y+w, -w, -w);
							}
						}
					}
				});
			}
		});	

// 		eraser tool
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
					}
				});
				aPane.setOnMouseDragReleased(new EventHandler<MouseDragEvent>() {	
					public void handle(MouseDragEvent event) {
						textPane.getChildren().clear();
					}
				});
			}
		});
		
//		text tool
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
								graph.fillText(text.getText(), x+fontSize*0.65, y+fontSize*1.33);
								count = 2;
								
							}							
					}
				});
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
		toolBox.getChildren().addAll(randomLineButton, lineButton, rectangleButton, roundButton, eraserButton, textButton, separateLine1, slider, separateLine2);			//add all the tool buttons here
//		the label on the right		
		Label label1 = new Label("		    Members list");
		Label label2 = new Label("		       Dialogue");
		
//		the windows on the right
		TextArea messageWindow = new TextArea();
		TextArea communicationWindow = new TextArea();
		TextArea inputWindow = new TextArea();
		communicationWindow.setWrapText(true);		//line break
		inputWindow.setPrefSize(240, 70);
		inputWindow.setPromptText("Input text here");
		inputWindow.setFocusTraversable(false);		
		communicationWindow.setPrefHeight(420);
		Button inputButton = new Button("<-");
		inputButton.setPrefSize(70, 70);
		HBox inputWinBox = new HBox();
		inputWinBox.setSpacing(5);
		inputWinBox.getChildren().addAll(inputWindow, inputButton);		//the input window and input button
		
// 		input part
		inputButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				communicationWindow.appendText(inputWindow.getText() + "\r\n");
			}
		});
		
		
//		communication and input windows are on this panel
		VBox windowPane = new VBox();
		windowPane.setPrefWidth(280);
		windowPane.setStyle("-fx-background-color:#ADD8E6;");	
		windowPane.getChildren().addAll(label1, messageWindow, label2, communicationWindow, inputWinBox);
		windowPane.setPadding(new Insets(5.0));
		windowPane.setSpacing(5.0);
//		Rectangle frame = new Rectangle();
//		frame.setX(0);
//		frame.setY(0);
//		frame.setWidth(250);
//		frame.setHeight(400);
//		frame.setStroke(Color.LAVENDER);
//		frame.setStrokeWidth(10);
//		windowPane.getChildren().addAll(frame);	
		

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
		stage.setWidth(1500);
		stage.setMinHeight(500);
		stage.setMinWidth(800);
		stage.setTitle("IG Drawing Board");
		stage.show();
		
//		String rootPath = getClass().getResource("/").getFile().toString();
//		System.out.println(rootPath);
	
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
	
	public void saveAs(Canvas canvas, int q) {			//if q =1 the system will be closed after saving
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
				}catch(Exception e) {		
					alertLabel.setText("Sorry! Saving fails. Something wrong with the route or file name.");
					aPane.setTopAnchor(alertLabel, 150.0);
					aPane.setLeftAnchor(alertLabel, 50.0);
				}
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
	public void close(Canvas canvas) {
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
					saveAs(canvas, 1);		//1 means when finish the operation, the system will be closed				
				}
				else if(status) {
					save(canvas);
				}
				stage.close();
			}
		});
	}
	public void save(Canvas canvas) {
		if(!status)
			saveAs(canvas, 0);
		else {
			try {
				File file = new File(saveRoute);
				WritableImage image = canvas.snapshot(new SnapshotParameters(), null);
				ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
