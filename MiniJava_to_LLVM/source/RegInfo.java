public class RegInfo {
    String regName; // Register that holds the identifier
    String type;    // Type of the identifier at llvm form

// Constructor
RegInfo(String name, String llvmType){
    regName = name;
    type = llvmType;
}

// Accessors
String getRegister(){ return regName; }
String getType(){ return type; }

// Mutators
void setRegister(String name){ regName = name; }
void setType(String llvmType){ regName = llvmType; }

}