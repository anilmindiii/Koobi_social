package com.mualab.org.user.activity.booking.model;

import java.util.List;

/**
 * Created by mindiii on 22/1/19.
 */

public class BookingConfirmInfo {
    
    public String status;
    public String message;
    public List<DataBean> data;
    
    public static class DataBean {
     
        public int _id;
        public String title;
        public int serviceId;
        public int subServiceId;
        public int inCallPrice;
        public int outCallPrice;
        public String completionTime;
        public String description;
        public String bookingType;
        public boolean isStaff;
        public boolean incallStaff;
        public boolean outcallStaff;
        public boolean isSelected;
        
    }
}
