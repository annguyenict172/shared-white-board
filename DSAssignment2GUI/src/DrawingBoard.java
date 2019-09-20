import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class DrawingBoard extends Application{

	public static void main(String[] args) {
		launch(args);
	}

	public void start(Stage primaryStage) throws Exception {
		Canvas canvas = new Canvas(1245, 775);
		GraphicsContext graph = canvas.getGraphicsContext2D();
		
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
				canvas.setOnDragDetected(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent event) {
						canvas.startFullDrag();						//must have this method, if you want to have full drag operation
						x = event.getX();
						y = event.getY();
					}
				});
				canvas.setOnMouseDragOver(new EventHandler<MouseDragEvent>() {		
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
				canvas.setOnMouseDragReleased(new EventHandler<MouseDragEvent>() {		
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
				canvas.setOnDragDetected(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent event) {
						canvas.startFullDrag();						//must have this method, if you want to have full drag operation
						x1 = event.getX();
						y1 = event.getY();
					}
				});
				canvas.setOnMouseDragOver(new EventHandler<MouseDragEvent>() {		//we must have this method, although it do nothing
					public void handle(MouseDragEvent event) {
						
					}
				});
				canvas.setOnMouseDragReleased(new EventHandler<MouseDragEvent>() {	
					public void handle(MouseDragEvent event) {
						x2 = event.getX();
						y2 = event.getY();
						graph.strokeLine(x1, y1, x2, y2);
					}
				});
			}
		});
		
// 		rec tool
		Tooltip tip3 = new Tooltip("Rectangle tool");
		tip1.setFont(Font.font(15));	
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
				canvas.setOnDragDetected(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent event) {
						canvas.startFullDrag();						//must have this method, if you want to have full drag operation
						x = event.getX();
						y = event.getY();
					}
				});
				canvas.setOnMouseDragOver(new EventHandler<MouseDragEvent>() {		//we must have this method, although it do nothing
					public void handle(MouseDragEvent event) {
						
					}
				});
				canvas.setOnMouseDragReleased(new EventHandler<MouseDragEvent>() {	
					public void handle(MouseDragEvent event) {
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
		tip1.setFont(Font.font(15));	
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
				canvas.setOnDragDetected(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent event) {
						canvas.startFullDrag();						//must have this method, if you want to have full drag operation
						x = event.getX();
						y = event.getY();
					}
				});
				canvas.setOnMouseDragOver(new EventHandler<MouseDragEvent>() {		//we must have this method, although it do nothing
					public void handle(MouseDragEvent event) {
					}
				});
				canvas.setOnMouseDragReleased(new EventHandler<MouseDragEvent>() {	
					public void handle(MouseDragEvent event) {
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
									graph.strokeOval(x+w, y, h, h);
								else if(w<0 && h<0)
									graph.strokeOval(x+w, y+h, -h, -h);
							}
							else if((Math.abs(w)-Math.abs(h))<0) {
								if(w>0 && h>0)
									graph.strokeOval(x, y, w, w);
								else if(w>0 && h<0)
									graph.strokeOval(x, y+h, w, w);
								else if(w<0 && h>0)
									graph.strokeOval(x+w, y, -w, -w);
								else if(w<0 && h<0)
									graph.strokeOval(x+w, y+h, -w, -w);
							}
								
						}
					}
				});
			}
		});	

// 		eraser tool
		Tooltip tip5 = new Tooltip("Eraser tool");
		tip1.setFont(Font.font(15));	
		Button eraserButton = new Button("", new ImageView(new Image(getClass().getResourceAsStream("rubber.png"))));
		eraserButton.setTooltip(tip5);
		eraserButton.setPrefHeight(55);
		eraserButton.setPrefWidth(55);
		eraserButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			double x;
			double y;
			public void handle(MouseEvent event) {
				canvas.setOnDragDetected(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent event) {
						canvas.startFullDrag();						//must have this method, if you want to have full drag operation
						graph.setStroke(Color.WHITE);
						graph.setLineWidth(20);
						x = event.getX();
						y = event.getY();
					}
				});
				canvas.setOnMouseDragOver(new EventHandler<MouseDragEvent>() {		//we must have this method, although it do nothing
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
				canvas.setOnMouseDragReleased(new EventHandler<MouseDragEvent>() {	
					public void handle(MouseDragEvent event) {
					}
				});
			}
		});
		
//		Line width button
		VBox lineWidth = new VBox();
		Button width1 = new Button("", new ImageView(new Image(getClass().getResourceAsStream("w1.png"))));
		width1.setPrefSize(100, 25);
		Button width2 = new Button("", new ImageView(new Image(getClass().getResourceAsStream("w2.png"))));
		width2.setPrefSize(100, 25);
		width2.setFont(Font.font(5));
		Button width3 = new Button("", new ImageView(new Image(getClass().getResourceAsStream("w3.png"))));
		width3.setPrefSize(100, 25);
		width3.setFont(Font.font(10));
		lineWidth.getChildren().addAll(width1, width2, width3);
		width1.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				graph.setLineWidth(1);
			}
		});
		width2.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				graph.setLineWidth(5);
				System.out.println(graph.getLineWidth());
			}
		});
		width3.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				graph.setLineWidth(10);
			}
		});
		
//		all the tools on this panel
		Line separateLine1 = new Line(0,0,0,80);
		separateLine1.setStroke(Color.valueOf("#C0C0C0"));
		HBox toolBox = new HBox();
		toolBox.setPadding(new Insets(5.0));
		toolBox.setSpacing(5);					//space between tools
		toolBox.setAlignment(Pos.TOP_LEFT);		//all the tools are top-left
		toolBox.getChildren().addAll(randomLineButton, lineButton, rectangleButton, roundButton, eraserButton, separateLine1, lineWidth);			//add all the tool buttons here
	
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
		
		AnchorPane aPane = new AnchorPane();				//the canvas is on the aPane
		aPane.setStyle("-fx-background-color:#FFFFFF;");	//white color board	
		aPane.getChildren().addAll(canvas);					//children of aPane
		aPane.setTopAnchor(canvas, 0.0);			//the size of the aPane should be changed following the stage
		aPane.setLeftAnchor(canvas, 0.0);
		aPane.setRightAnchor(canvas, 0.0);
		aPane.setBottomAnchor(canvas, 0.0);
		
//	    root anchorPane
		AnchorPane root = new AnchorPane();
		root.setStyle("-fx-background-color:#F8F8FF;");	
		root.getChildren().addAll(aPane, toolBox, windowPane);
		root.setTopAnchor(aPane, 125.0);			//the size of the aPane should be changed following the stage
		root.setLeftAnchor(aPane, 5.0);
		root.setRightAnchor(aPane, 285.0);
		root.setBottomAnchor(aPane, 5.0);
		root.setRightAnchor(windowPane, 0.0);
		root.setBottomAnchor(windowPane, 0.0);
		root.setTopAnchor(windowPane, 125.0);
		root.setTopAnchor(toolBox, 30.0);
		root.setLeftAnchor(toolBox, 50.0);
		
		Scene scene = new Scene(root);
		
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.setHeight(900);
		stage.setWidth(1500);
		stage.setMinHeight(500);
		stage.setMinWidth(800);
		stage.setTitle("IG Drawing Board");
		
//		String rootPath = getClass().getResource("/").getFile().toString();
//		System.out.println(rootPath);
	
		stage.getIcons().add(new Image(getClass().getResourceAsStream("download.jpg")));		//set the icon of this app
		stage.show();
		
	}
	
}
