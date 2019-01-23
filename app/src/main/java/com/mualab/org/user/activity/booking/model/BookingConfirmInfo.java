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
        /**
         * _id : 87
         * bookingPrice : 55.0
         * * serviceId : 1
         * subServiceId : 1
         * artistServiceId : 6
         * bookingDate : 2019-01-23
         * startTime : 11:00 AM
         * endTime : 12:10 PM
         * artistServiceName : mango Gghhvb Hjjjjjj Hhjjjnjnn
         * staffId : 5
         * staffName : deepu
         * staffImage : http://koobi.co.uk:3000/uploads/profile/
         * companyId : 5
         * companyName : development
         * companyAddress : MINDIII Systems Pvt. Ltd., 502, 503 & 504 Krishna Tower Above ICICI Bank, Main Rd, Brajeshwari Extension, Pipliyahana, Indore, Madhya Pradesh 452016, India
         * companyImage : http://koobi.co.uk:3000/uploads/profile/
         */
        public int _id;
        public String bookingPrice;
        public int serviceId;
        public int subServiceId;
        public int artistServiceId;
        public String bookingDate;
        public String startTime;
        public String endTime;
        public String artistServiceName;
        public int staffId;
        public String staffName;
        public String staffImage;
        public int companyId;
        public String companyName;
        public String companyAddress;
        public String companyImage;
    }
}
