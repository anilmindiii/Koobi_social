package com.mualab.org.user.activity.booking.model;

import java.util.List;

/**
 * Created by mindiii on 30/1/19.
 */

public class BookingListInfo {

    public String status;
    public String message;
    public DataBean data;

    public static class DataBean {
        public int _id;
        public String discountPrice;
        public String bookingDate;
        public String customerType;
        public int bookingType;
        public String bookingTime;
        public String bookStatus;
        public String location;
        public int paymentType;
        public int paymentStatus;
        public VoucherBean voucher;
        public int timeCount;
        public String paymentTime;
        public int artistId;
        public String totalPrice;
        public List<UserDetailBean> userDetail;
        public List<ArtistDetailBean> artistDetail;
        public List<BookingInfoBean> bookingInfo;

        public static class VoucherBean {

            public String voucherCode;
            public String endDate;
            public String artistId;
            public String deleteStatus;
            public String __v;
            public String _id;
            public String status;
            public String startDate;
            public String amount;
            public String discountType;
            
        }

        public static class UserDetailBean {
            
            public int _id;
            public String userName;
            public String profileImage;

          
        }

        public static class ArtistDetailBean {

            public int _id;
            public String userName;
            public String profileImage;
            
        }

        public static class BookingInfoBean {

            public int _id;
            public String bookingPrice;
            public int serviceId;
            public int subServiceId;
            public int artistServiceId;
            public String bookingDate;
            public String startTime;
            public String endTime;
            public int status;
            public String artistServiceName;
            public int staffId;
            public String staffName;
            public String staffImage;
            public String trackingLatitude;
            public String trackingLongitude;
            public int companyId;
            public String companyName;
            public String companyImage;
            
        }
    }
}
