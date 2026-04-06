package com.archivonegativoscronica;


import java.util.List;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public final class TablaHelper {

    private TablaHelper() {}

    public static <R> TableView<R> crearTabla(List<ColumnaConfig> columnas) {
        TableView<R> table = new TableView<>();

        configurarColumnas(table, columnas);

        // importante: dejamos que respete los prefWidth
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        return table;
    }

    public static <R> void configurarColumnas(TableView<R> table, List<ColumnaConfig> columnas) {
        table.getColumns().clear();
        for (ColumnaConfig cfg : columnas) {
            TableColumn<R, Object> col = new TableColumn<>(cfg.getHeader());
            col.setCellValueFactory(new PropertyValueFactory<>(cfg.getProperty()));

            if (cfg.getWidth() > 0) {
                col.setPrefWidth(cfg.getWidth());
            }

            table.getColumns().add(col);
        }
    }
}

