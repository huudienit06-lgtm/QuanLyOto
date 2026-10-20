package gui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;
import javax.swing.JComboBox;

public final class FormValidator {
    public static final String REGEX_MA_DINH_DANH = "^[A-Za-z0-9_-]{1,20}$";
    public static final String REGEX_HO_TEN = "^[\\p{L}\\p{M}]+(?:[ .'-][\\p{L}\\p{M}]+)*$";
    public static final String REGEX_SO_DIEN_THOAI = "^(?:\\+?84|0)[1-9][0-9]{8,9}$";
    public static final String REGEX_EMAIL =
            "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?"
                    + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$";
    public static final String REGEX_BIEN_SO_XE = "^[0-9]{2}[A-Z]{1,2}-?[0-9]{4,5}(?:\\.[0-9]{2})?$";
    public static final String REGEX_SO_NGUYEN = "^-?[0-9]+$";
    public static final String REGEX_SO_THUC = "^-?[0-9]+(?:[.,][0-9]+)?$";
    public static final String REGEX_NGAY_ISO = "^[0-9]{4}-[0-9]{2}-[0-9]{2}$";
    public static final String REGEX_GIO = "^[0-9]{2}:[0-9]{2}$";

    // private static final Pattern MA_DINH_DANH_PATTERN = Pattern.compile(REGEX_MA_DINH_DANH);
    private static final Pattern HO_TEN_PATTERN = Pattern.compile(REGEX_HO_TEN);
    private static final Pattern SO_DIEN_THOAI_PATTERN = Pattern.compile(REGEX_SO_DIEN_THOAI);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(REGEX_EMAIL);
    private static final Pattern BIEN_SO_XE_PATTERN = Pattern.compile(REGEX_BIEN_SO_XE);
    private static final Pattern SO_NGUYEN_PATTERN = Pattern.compile(REGEX_SO_NGUYEN);
    private static final Pattern SO_THUC_PATTERN = Pattern.compile(REGEX_SO_THUC);
    private static final Pattern NGAY_ISO_PATTERN = Pattern.compile(REGEX_NGAY_ISO);
    private static final Pattern GIO_PATTERN = Pattern.compile(REGEX_GIO);

    private FormValidator() {
    }

    public static boolean isRequired(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean matches(String value, String regex) {
        return value != null && regex != null && value.trim().matches(regex);
    }

    public static boolean isValidIdentifier(String value) {
        return matches(value, REGEX_MA_DINH_DANH);
    }

    public static boolean isValidFullName(String value) {
        return value != null && HO_TEN_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean isValidPhoneNumber(String value) {
        return value != null && SO_DIEN_THOAI_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean isValidOptionalPhoneNumber(String value) {
        return !isRequired(value) || isValidPhoneNumber(value);
    }

    public static boolean isValidEmail(String value) {
        return value != null && EMAIL_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean isValidOptionalEmail(String value) {
        return !isRequired(value) || isValidEmail(value);
    }

    public static boolean isValidLicensePlate(String value) {
        return value != null && BIEN_SO_XE_PATTERN.matcher(value.trim().toUpperCase()).matches();
    }

    public static boolean isValidInteger(String value) {
        return value != null && SO_NGUYEN_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean isValidDecimal(String value) {
        return value != null && SO_THUC_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean isValidIsoDate(String value) {
        if (value == null || !NGAY_ISO_PATTERN.matcher(value.trim()).matches()) {
            return false;
        }
        try {
            LocalDate.parse(value.trim());
            return true;
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    public static boolean isValidTime(String value) {
        if (value == null || !GIO_PATTERN.matcher(value.trim()).matches()) {
            return false;
        }
        try {
            LocalTime.parse(value.trim());
            return true;
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    public static boolean isValidComboBoxSelection(JComboBox<?> comboBox) {
        if (comboBox == null || comboBox.getSelectedItem() == null || comboBox.getSelectedIndex() < 0) {
            return false;
        }
        String selectedText = comboBox.getSelectedItem().toString().trim();
        String normalizedText = selectedText.toLowerCase(java.util.Locale.ROOT);
        return !selectedText.isEmpty()
                && !normalizedText.startsWith("chọn ")
                && !normalizedText.startsWith("vui lòng chọn")
                && !selectedText.startsWith("--");
    }

    public static boolean isValidComboBoxSelection(JComboBox<?> comboBox, Object placeholder) {
        return isValidComboBoxSelection(comboBox)
                && !java.util.Objects.equals(comboBox.getSelectedItem(), placeholder);
    }
}
