module com.example.wavern {
    requires javafx.controls;
    requires javafx.fxml;
    requires LeapJava;


    opens com.example.wavern to javafx.fxml;
    exports com.example.wavern;
}