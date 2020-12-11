/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaprojectserver;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;

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
    private ListView<String> onlineList;
    @FXML
    private ListView<String> offlineList;
    @FXML
    private ListView <String>activeList;
    @FXML
    private Text onlineTotalText;
    @FXML
    private Text offlineTotalText;
    @FXML
    private Text activeTotalText;
    @FXML
    private Text ipText;
    @FXML
    private PieChart pieChart;
    
    
    @FXML
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        XoServer server=new XoServer();
        ipText.setText(server.getIP());
        ObservableList<String> listOnline=FXCollections.observableArrayList( server.db.getOnlineUsers());
        ObservableList<String> listOffline = FXCollections.observableArrayList(server.db.getOfflineUsers());
        ObservableList<String> listActive = FXCollections.observableArrayList(server.db.getActiveUsers());

        onlineList.setItems(listOnline);
        offlineList.setItems(listOffline);
        activeList.setItems(listActive);
        
         ObservableList<PieChart.Data> pieChartData
                = FXCollections.observableArrayList(
                new PieChart.Data("Offline",listOffline.size()),
                new PieChart.Data("Online", listOnline.size()),
                new PieChart.Data("Active", listActive.size()) );

        pieChart.setData(pieChartData);
        pieChart.getStyle();
        pieChart.setTitle("Chart show Active,Online And Offline Users ");
        pieChart.setMaxSize(800, 800);
        pieChart.setLabelLineLength(10);
        pieChart.setLegendSide(Side.LEFT);
      
       
        // TODO
    }  
    
        
    
  
    
}
