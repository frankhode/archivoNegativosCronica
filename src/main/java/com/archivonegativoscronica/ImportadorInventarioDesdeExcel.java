package com.archivonegativoscronica;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImportadorInventarioDesdeExcel {

    // Encabezados esperados en Excel (los comparo ignoreCase)
    public static final String COL_BARCODE = "Barcode";
    public static final String COL_UFI = "Ubicación física";
    public static final String COL_NRO_ORIGINAL = "Nro. Original";
    public static final String COL_TITULO = "Título";
    public static final String COL_FECHA = "Fecha";
    public static final String COL_FOTOGRAFO = "Fotógrafo";

    private static final Pattern NRO_PREFIX = Pattern.compile("^\\s*(\\d+)(.*)$");
    private static final DateTimeFormatter FECHA_DDMMYYYY = DateTimeFormatter.ofPattern("d/M/uuuu", new Locale("es", "AR"));
    private static final DateTimeFormatter FECHA_ISO_YYYYMMDD = DateTimeFormatter.ofPattern("uuuuMMdd");

    public static class InventarioRow {
        public int excelRowNumber; // 1-based (humano)
        public String barcode;
        public String ufi;
        public String nroA;       // Nro. Original normalizado
        public String titulo;
        public String fechaISO;   // yyyyMMdd
        public String autor;
        public String error;      // si algo está mal

        public boolean ok() { return error == null || error.isBlank(); }
    }

    /** Lee y devuelve preview (primeras maxRows filas) */
    public List<InventarioRow> preview(File xlsxFile, int headerRow, int maxRows) throws Exception {
        Objects.requireNonNull(xlsxFile, "xlsxFile");
        if (!xlsxFile.exists()) throw new IllegalArgumentException("Excel inexistente: " + xlsxFile);

        List<InventarioRow> out = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(xlsxFile);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) throw new IllegalStateException("El Excel no tiene hojas.");

            Row header = sheet.getRow(headerRow);
            if (header == null) throw new IllegalStateException("No encontré encabezados en row=" + headerRow);

            int cBarcode = findColumnIndex(header, COL_BARCODE);
            int cUfi = findColumnIndex(header, COL_UFI);
            int cNro = findColumnIndex(header, COL_NRO_ORIGINAL);
            int cTitulo = findColumnIndex(header, COL_TITULO);
            int cFecha = findColumnIndex(header, COL_FECHA);
            int cFoto = findColumnIndex(header, COL_FOTOGRAFO);

            DataFormatter fmt = new DataFormatter(Locale.ROOT);

            int last = sheet.getLastRowNum();
            int count = 0;

            for (int r = headerRow + 1; r <= last; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                InventarioRow ir = new InventarioRow();
                ir.excelRowNumber = r + 1;

                ir.barcode = getCellString(fmt, row.getCell(cBarcode));
                ir.ufi = getCellString(fmt, row.getCell(cUfi));
                ir.nroA = normalizeNroOriginalPad6(getCellString(fmt, row.getCell(cNro)));
                ir.titulo = getCellString(fmt, row.getCell(cTitulo));
                ir.fechaISO = normalizeFechaISO(row.getCell(cFecha), fmt);
                ir.autor = getCellString(fmt, row.getCell(cFoto));

                // Validaciones mínimas
                if (ir.barcode == null || ir.barcode.isBlank()) {
                    ir.error = "Barcode vacío";
                }

                out.add(ir);

                count++;
                if (maxRows > 0 && count >= maxRows) break;
            }
        }

        return out;
    }

    /** Importa TODO (no preview), con modo simulación opcional */
    public ImportResult importar(Connection conn, File xlsxFile, int headerRow, boolean dryRun) throws Exception {
        Objects.requireNonNull(conn, "conn");
        Objects.requireNonNull(xlsxFile, "xlsxFile");
        if (!xlsxFile.exists()) throw new IllegalArgumentException("Excel inexistente: " + xlsxFile);

        ImportResult res = new ImportResult();

        String sqlUpsert =
                "INSERT INTO inventario (barcode, ufi, nroA, titulo, fechaISO, autor) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "ufi = VALUES(ufi), " +
                "nroA = VALUES(nroA), " +
                "titulo = VALUES(titulo), " +
                "fechaISO = VALUES(fechaISO), " +
                "autor = VALUES(autor)";

        boolean oldAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try (PreparedStatement ps = conn.prepareStatement(sqlUpsert);
             FileInputStream fis = new FileInputStream(xlsxFile);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            Row header = sheet.getRow(headerRow);
            if (header == null) throw new IllegalStateException("No encontré encabezados en row=" + headerRow);

            int cBarcode = findColumnIndex(header, COL_BARCODE);
            int cUfi = findColumnIndex(header, COL_UFI);
            int cNro = findColumnIndex(header, COL_NRO_ORIGINAL);
            int cTitulo = findColumnIndex(header, COL_TITULO);
            int cFecha = findColumnIndex(header, COL_FECHA);
            int cFoto = findColumnIndex(header, COL_FOTOGRAFO);

            DataFormatter fmt = new DataFormatter(Locale.ROOT);

            int last = sheet.getLastRowNum();
            int batchCount = 0;

            for (int r = headerRow + 1; r <= last; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String barcode = getCellString(fmt, row.getCell(cBarcode));
                if (barcode == null || barcode.isBlank()) {
                    res.errores++;
                    continue;
                }

                String ufi = getCellString(fmt, row.getCell(cUfi));
                String nroA = normalizeNroOriginalPad6(getCellString(fmt, row.getCell(cNro)));
                String titulo = getCellString(fmt, row.getCell(cTitulo));
                String fechaISO = normalizeFechaISO(row.getCell(cFecha), fmt);
                String autor = getCellString(fmt, row.getCell(cFoto));

                res.leidas++;

                if (!dryRun) {
                    setNotNullString(ps, 1, barcode.trim());
                    setNotNullString(ps, 2, ufi);
                    setNotNullString(ps, 3, nroA);
                    setNotNullString(ps, 4, titulo);
                    setNotNullString(ps, 5, fechaISO);
                    setNotNullString(ps, 6, autor);
if (res.leidas <= 3) {
    System.out.println("DEBUG -> " +
        "ufi=[" + (ufi == null ? "NULL" : ufi) + "] " +
        "nroA=[" + (nroA == null ? "NULL" : nroA) + "] " +
        "titulo=[" + (titulo == null ? "NULL" : titulo) + "] " +
        "fechaISO=[" + (fechaISO == null ? "NULL" : fechaISO) + "] " +
        "autor=[" + (autor == null ? "NULL" : autor) + "]"
    );
}

                    ps.addBatch();
                    batchCount++;

                    if (batchCount % 500 == 0) {
                        ps.executeBatch();
                        conn.commit();
                    }
                }
            }

            if (!dryRun) {
                ps.executeBatch();
                conn.commit();
            }

        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(oldAutoCommit);
        }

        return res;
    }

    public static class ImportResult {
        public int leidas;
        public int errores;
    }

    // ---------------- helpers ----------------

    private static int findColumnIndex(Row headerRow, String expectedHeader) {
        DataFormatter fmt = new DataFormatter(Locale.ROOT);
        for (Cell cell : headerRow) {
            String val = fmt.formatCellValue(cell);
            if (val != null && val.trim().equalsIgnoreCase(expectedHeader.trim())) {
                return cell.getColumnIndex();
            }
        }
        throw new IllegalStateException("No encontré la columna '" + expectedHeader + "' en el encabezado.");
    }

    private static String getCellString(DataFormatter fmt, Cell cell) {
        if (cell == null) return null;
        String v = fmt.formatCellValue(cell);
        return v != null ? v.trim() : null;
    }

    public static String normalizeNroOriginalPad6(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;

        Matcher m = NRO_PREFIX.matcher(s);
        if (!m.matches()) return s;

        String num = m.group(1);
        String rest = m.group(2) != null ? m.group(2).trim() : "";

        String padded = String.format("%06d", safeParseInt(num));
        return rest.isEmpty() ? padded : padded + " " + rest;
    }

    private static int safeParseInt(String s) {
        try { return Integer.parseInt(s); }
        catch (Exception e) { return 0; }
    }

    public static String normalizeFechaISO(Cell cell, DataFormatter fmt) {
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            LocalDate ld = cell.getLocalDateTimeCellValue().toLocalDate();
            return FECHA_ISO_YYYYMMDD.format(ld);
        }

        String s = fmt.formatCellValue(cell);
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;

        try {
            LocalDate ld = LocalDate.parse(s, FECHA_DDMMYYYY);
            return FECHA_ISO_YYYYMMDD.format(ld);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static void setNotNullString(PreparedStatement ps, int idx, String value) throws SQLException {
        ps.setString(idx, value == null ? "" : value.trim());
    }

}
