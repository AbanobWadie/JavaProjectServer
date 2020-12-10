/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaprojectserver;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author Alshaimaa
 */
public class StartServerController implements Initializable {
    
   
     @FXML
    Button btnStartServer = new Button();
     
      
    @FXML
    private void startServer(ActionEvent event) {
            
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {

           btnStartServer.setOnAction(new EventHandler<ActionEvent>(){
        
          @Override
          public void handle(ActionEvent e) {
              try {
                 Parent root = FXMLLoader.load(getClass().getResource("ServerRun.fxml"));
                 Scene scene = new Scene(root);
                 Stage stage=(Stage)((Node)e.getSource()).getScene().getWindow();
                 stage.setScene(scene);
                 stage.show();
            } catch (IOException ex) {
                Logger.getLogger(StartServerController.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    });
        
        
        // TODO
    }  

    
}
