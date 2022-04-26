package dep8.ijse.lk.dto;

import java.io.Serializable;
import java.util.Arrays;

public class BookDT02 implements Serializable {
    private String id;
    private String name;
    private String author;
    private String type;
    private byte[] preview;

    public BookDT02(String id, String name, String author, String type, byte[] preview) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.type = type;
        this.preview = preview;
    }

    public BookDT02() {
    }

    public BookDT02(String id, String name, String author, String type) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte[] getPreview() {
        return preview;
    }

    public void setPreview(byte[] preview) {
        this.preview = preview;
    }

    @Override
    public String toString() {
        return "BookDT02{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", type='" + type + '\'' +
                ", preview=" + Arrays.toString(preview) +
                '}';
    }
}
