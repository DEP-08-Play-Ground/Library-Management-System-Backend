package dep8.ijse.lk.dto;

import java.io.Serializable;

public class IssueBookDTO implements Serializable{
    private String issueId;
    private String bookId;
    private String memberId;
    private String datetime;


    public IssueBookDTO(String issueId, String bookId, String memberId, String datetime) {
        this.issueId = issueId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.datetime = datetime;
    }

    public IssueBookDTO(String issueId, String bookId, String memberId) {
        this.issueId = issueId;
        this.bookId = bookId;
        this.memberId = memberId;
    }

    public IssueBookDTO() {
    }

    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
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

    @Override
    public String toString() {
        return "IssueBookDTO{" +
                "issueId='" + issueId + '\'' +
                ", bookId='" + bookId + '\'' +
                ", memberId='" + memberId + '\'' +
                ", datetime='" + datetime + '\'' +
                '}';
    }
    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }
}
