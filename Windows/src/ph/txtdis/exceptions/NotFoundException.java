package ph.txtdis.exceptions;

import ph.txtdis.windows.Type;

public class NotFoundException extends NullPointerException {
    private static final long serialVersionUID = 7722739983925870751L;
    private int id;
    private Type type;

    public NotFoundException(Type type, int id) {
        super();
        this.type = type;
        this.id = id;
    }

 	@Override
    public String toString() {
 		String name = type.getName().replace(" Data", "");
        return "There is no record of\n" + name + " #" + id;
    }
}
