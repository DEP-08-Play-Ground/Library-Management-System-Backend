package dep8.ijse.lk.dto;

public class returnDTO {
    private String returnId;
    private String bookId;
    private String memberId;
    private String dateTime;

    public returnDTO(String returnId, String bookId, String memberId, String dateTime) {
        this.returnId = returnId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.dateTime = dateTime;
    }

    public returnDTO() {
    }

    public String getReturnId() {
        return returnId;
    }

    public void setReturnId(String returnId) {
        this.returnId = returnId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "returnDTO{" +
                "returnId='" + returnId + '\'' +
                ", bookId='" + bookId + '\'' +
                ", memberId='" + memberId + '\'' +
                ", dateTime='" + dateTime + '\'' +
                '}';
    }
}
