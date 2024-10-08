package com.personal.transaction.storage.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static final SimpleDateFormat validDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static Long validateDate(String transactionDate) throws ParseException {
        Date toDate = validDateFormat.parse(transactionDate);
        return toDate.getTime();
    }
}
