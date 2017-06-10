package ru.ewromet.converter3;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import ru.ewromet.Controller;
import ru.ewromet.converter2.Controller2;

public class Controller3 extends Controller {

    @FXML
    private TableView table1;

    @FXML
    private TableView table2;

    private Controller2 controller1;

    public void setController1(Controller2 controller1) {
        this.controller1 = controller1;
    }
}
