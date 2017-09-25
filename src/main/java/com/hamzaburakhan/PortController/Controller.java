package com.hamzaburakhan.PortController;

import com.hamzaburakhan.PortController.entity.PortsAndProcess;
import com.hamzaburakhan.PortController.utils.PortScanner;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.controlsfx.dialog.Dialogs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public class Controller implements Initializable{

    private  List<PortsAndProcess> listportandprocess = new ArrayList<>();

    public TableView<PortsAndProcess> porttable;

    public ObservableList<PortsAndProcess> filteredList;
    public BorderPane borderPaneApp;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filteredList =  FXCollections.observableArrayList(listportandprocess);
        porttable.setItems(filteredList);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    startTask();
                }
                catch (Exception e){
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }

                    startTask();
                }
            }
        });


    }

    public void startTask(){
        Service<Void> findMyDeviceService = new SearchPorts();
        Dialogs.create()
                .owner((Stage) borderPaneApp.getScene().getWindow())
                .title("Portlar Bulunuyor")
                .masthead("Portlar Bulunuyor")
                .showWorkerProgress(findMyDeviceService);

        findMyDeviceService.start();
    }
    class SearchPorts extends Service<Void> {


        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call()   {
                    try {
                        updateMessage("Portlar Aranıyor");
                        String line = null;

                        Process p = null;

                        p = Runtime.getRuntime().exec("netstat -a -n -o");

                        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));

                        int proggresslimit = 0;
                        while (true) {
                            line = r.readLine();
                            if (line == null) {
                                break;
                            }
                            proggresslimit++;
                        }

                        updateProgress(0, proggresslimit);
                        p = Runtime.getRuntime().exec("netstat -a -n -o");
                        r = new BufferedReader(new InputStreamReader(p.getInputStream()));

                        listportandprocess = new ArrayList<>();
                        boolean contentReady = false;
                        int count = 0;
                        while (true) {
                            try {
                                line = r.readLine();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (line == null) {
                                break;
                            }

                            if(contentReady) {
                                PortsAndProcess portsAndProcess = new PortsAndProcess();
                                String splitArray[] = line.split(" ");
                                int colum = 0;
                                for (int i = 0; i < splitArray.length; i++) {
                                    String result = splitArray[i].replace(" ", "");
                                    if (!result.equals("")) {
                                        switch (colum) {
                                            case 0:
                                                portsAndProcess.setProtocol(result);
                                                break;
                                            case 1:
                                                portsAndProcess.setLocalAddress(result);
                                                break;
                                            case 2:
                                                portsAndProcess.setForeignAddress(result);
                                                break;
                                            case 3:
                                                portsAndProcess.setState(result);
                                                break;
                                            case 4:
                                                portsAndProcess.setPID(result);
                                                break;
                                        }
                                        colum++;
                                    }
                                }
                                listportandprocess.add(portsAndProcess);
                                filteredList.add(portsAndProcess);

                            }
                            if(line.contains("PID")){
                                contentReady = true;
                            }

                            updateMessage("PID adları getiriliyor "+count+"/"+proggresslimit);
                            updateProgress(count, proggresslimit);
                            count++;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    porttable.setRowFactory(tv -> {
                        TableRow<PortsAndProcess> row = new TableRow<>();
                        row.setOnMouseClicked(event -> {
                            if (! row.isEmpty() && event.getButton()== MouseButton.PRIMARY
                                    && event.getClickCount() == 2) {

                                PortsAndProcess clickedRow = row.getItem();
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setTitle("Kill To Process");
                                alert.setHeaderText("Kill To Process");
                                alert.setContentText("Are you sure to kill the process?");

                                Optional<ButtonType> result = alert.showAndWait();
                                if (result.get() == ButtonType.OK){
                                    try {
                                        Process p = Runtime.getRuntime().exec("taskkill /F /PID "+clickedRow.getPID());
                                        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                        while (true) {
                                            String line = r.readLine();

                                            if (line == null) {
                                                break;
                                            }
                                        }

                                        filteredList.clear();
                                        startTask();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    // ... user chose CANCEL or closed the dialog
                                }
                            }
                        });
                        return row ;
                    });



                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            filteredList.forEach(item-> {
                                try
                                {
                                    String pid = item.getPID();
                                    Process p = Runtime.getRuntime().exec("cmd /c \n" +
                                            "for /f %a in ( 'tasklist /fi \"PID eq " + pid + "\" ^| findstr \"" + pid + "\"' ) do (echo %a)");
                                    BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                    int count = 0;
                                    while (true) {
                                        String line = r.readLine();

                                        if (line == null) {
                                            break;
                                        }
                                        if (count == 2) {
                                            item.setProccessName(line);
                                        }
                                        count++;
                                    }
                                    porttable.refresh();
                                }catch(Exception e)

                                {
                                    e.printStackTrace();
                                }

                            });
                        }
                    }).start();
                    return  null;
                }
            };
        }
    }
}
