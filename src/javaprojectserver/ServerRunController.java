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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Alshaimaa
 */
public class ServerRunController implements Initializable {

    /**
     * Initializes the controller class.
     */
    
    @FXML
    private Label online;
    @FXML
    private Label offline;
    @FXML
    private Label active;
    @FXML
    private Button stopServer;
    @FXML
    private ListView onlineList;
    @FXML
    private ListView offlineList;
    @FXML
    private ListView activeList;
    @FXML
    private Text onlineTotalText;
    @FXML
    private Text offlineTotalText;
    @FXML
    private Text activeTotalText;
    @FXML
    private Text ipText;
      
    @FXML
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }  
    
  
    
}
