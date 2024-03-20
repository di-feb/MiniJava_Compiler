public class VarInfo {
    private String type;        // Type of the var
    private Integer offset;     // Offset of the var

    // Accessors
    public String getType(){ return type; }
    public int getOffset() { return offset; }

    // Mutators
    public void setType(String _type_){ this.type = _type_; }
    public void setOffset(Integer _offset_){ this.offset = _offset_; }
}