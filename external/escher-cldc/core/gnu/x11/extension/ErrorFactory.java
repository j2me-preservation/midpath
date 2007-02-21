package gnu.x11.extension;


public interface ErrorFactory {
  gnu.x11.Error build (gnu.x11.Display display, gnu.x11.Data data, 
    int code, int seq_no, int bad, int minor_opcode, int major_opcode);
}
