package yandex.com.mds.hw.network;

public class NoteServiceResponse<T> {
    private String status;
    private T data;

    public NoteServiceResponse(String status, T data) {
        this.status = status;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "NoteServiceResponse{" +
                "status='" + status + '\'' +
                ", data=" + data +
                '}';
    }
}
