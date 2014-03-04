package org.llvm;

import java.util.concurrent.atomic.AtomicReference;

import org.bridj.Pointer;
import static org.llvm.binding.LLVMLibrary.*;

/**
 * The main container class for the LLVM Intermediate Representation.
 */
public class Module {

	private LLVMModuleRef module;

	public LLVMModuleRef module() {
		return this.module;
	}

	public Module(LLVMModuleRef module) {
		this.module = module;
	}

	/**
	 * Create a new, empty module in the global context.<br>
	 * This is equivalent to calling LLVMModuleCreateWithNameInContext with<br>
	 * LLVMGetGlobalContext() as the context parameter.<br>
	 * Every invocation should be paired with LLVMDisposeModule() or memory<br>
	 * will be leaked.
	 */
	public static Module createWithName(String moduleID) {
		Pointer<Byte> cstr = Pointer.pointerToCString(moduleID);
		return new Module(LLVMModuleCreateWithName(cstr));
	}

	/**
	 * Create a new, empty module in a specific context.<br>
	 * Every invocation should be paired with LLVMDisposeModule() or memory<br>
	 * will be leaked.
	 */
	public static Module createWithNameInContext(String moduleID, Context c) {
		Pointer<Byte> cstr = Pointer.pointerToCString(moduleID);
		return new Module(LLVMModuleCreateWithNameInContext(cstr, c.context()));
	}

	@Override
	public void finalize() {
		this.dispose();
	}

	/**
	 * Destroy a module instance.<br>
	 * This must be called for every created module or memory will be<br>
	 * leaked.
	 */
	public void dispose() {
		LLVMDisposeModule(this.module);
		this.module = null;
	}

	/**
	 * Verifies that a module is valid, throwing an exception if not.
	 */
	public void verify() throws LLVMException {
		Pointer<Pointer<Byte>> ppByte = Pointer.pointerToCStrings("");
		int retval = LLVMVerifyModule(this.module,
				LLVMVerifierFailureAction.LLVMReturnStatusAction, ppByte);
		if (retval != 0) {
			Pointer<Byte> pByte = ppByte.getPointer(Byte.class);
			final String message = pByte.getCString();
			LLVMDisposeMessage(pByte);
			throw new LLVMException(message);
		}
	}

	public static void disposeMessage(AtomicReference<String> message) {

	}

	/**
	 * Obtain the data layout for a module.<br>
	 * 
	 * @see Module::getDataLayout()
	 */
	public String getDataLayout() {
		Pointer<Byte> cstr = LLVMGetDataLayout(this.module);
		return cstr.getCString();
	}

	/**
	 * Set the data layout for a module.<br>
	 * 
	 * @see Module::setDataLayout()
	 */
	public void setDataLayout(String triple) {
		Pointer<Byte> cstr = Pointer.pointerToCString(triple);
		LLVMSetDataLayout(this.module, cstr);
	}

	/**
	 * Obtain the target triple for a module.<br>
	 * 
	 * @see Module::getTargetTriple()
	 */
	public String getTarget() {
		Pointer<Byte> cstr = LLVMGetTarget(this.module);
		return cstr.getCString();
	}

	/**
	 * Set the target triple for a module.<br>
	 * 
	 * @see Module::setTargetTriple()
	 */
	public void setTarget(String triple) {
		Pointer<Byte> cstr = Pointer.pointerToCString(triple);
		LLVMSetTarget(this.module, cstr);
	}

	/* public int addTypeName(String name, LLVMTypeRef ty) {
	 * Pointer<Byte> cstr = Pointer.pointerToCString(name);
	 * return LLVMAddTypeName(module, cstr, ty);
	 * }
	 * public void deleteTypeName(String name) {
	 * Pointer<Byte> cstr = Pointer.pointerToCString(name);
	 * LLVMDeleteTypeName(module, cstr);
	 * } */

	/**
	 * Obtain a Type from a module by its registered name.
	 */
	public TypeRef getTypeByName(String name) {
		return new TypeRef(LLVMGetTypeByName(this.module,
				Pointer.pointerToCString(name)));
	}

	/* public String getTypeName(LLVMTypeRef ty) {
	 * Pointer<Byte> cstr = LLVMGetTypeName(module, ty);
	 * return cstr.getCString();
	 * } */

	/**
	 * Dump a representation of a module to stderr.<br>
	 * 
	 * @see Module::dump()
	 */
	public void dumpModule() {
		LLVMDumpModule(this.module);
	}

	/**
	 * Writes a module to the specified path. Returns 0 on success.
	 */
	public int writeBitcodeToFile(String path) {
		return LLVMWriteBitcodeToFile(this.module,
				Pointer.pointerToCString(path));
	}

	/**
	 * Set inline assembly for a module.<br>
	 * 
	 * @see Module::setModuleInlineAsm()
	 */
	public void setModuleInlineAsm(String asm) {
		Pointer<Byte> cstr = Pointer.pointerToCString(asm);
		LLVMSetModuleInlineAsm(this.module, cstr);
	}

	public Context getModuleContext() {
		return Context.getModuleContext(this);
	}

	public Value addGlobal(TypeRef ty, String name) {
		return new Value(LLVMAddGlobal(this.module(), ty.type(),
				Pointer.pointerToCString(name)));
	}

	public Value addGlobalInAddressSpace(TypeRef ty, String name,
			int AddressSpace) {
		return new Value(LLVMAddGlobalInAddressSpace(this.module(), ty.type(),
				Pointer.pointerToCString(name), AddressSpace));
	}

	public Value getNamedGlobal(String name) {
		return new Value(LLVMGetNamedGlobal(this.module(),
				Pointer.pointerToCString(name)));
	}

	public Value getFirstGlobal() {
		return new Value(LLVMGetFirstGlobal(this.module()));
	}

	public Value getLastGlobal() {
		return new Value(LLVMGetLastGlobal(this.module()));
	}

	public Value addAlias(TypeRef ty, Value aliasee, String name) {
		return new Value(LLVMAddAlias(this.module, ty.type(), aliasee.value(),
				Pointer.pointerToCString(name)));
	}

	/**
	 * Add a function to a module under a specified name.
	 * 
	 * @see llvm::Function::Create()
	 */
	public Value addFunction(String name, TypeRef functionTy) {
		return new Value(LLVMAddFunction(this.module,
				Pointer.pointerToCString(name), functionTy.type()));
	}

	public Value addOrInsertFunction(String name, TypeRef functionTy) {
		try {
			return this.getNamedFunction(name);
		} catch (LLVMException e) {
			return this.addFunction(name, functionTy);
		}
	}

	public Value addFunction(String name, LLVMTypeRef functionTy) {
		return new Value(LLVMAddFunction(this.module,
				Pointer.pointerToCString(name), functionTy));
	}

	/**
	 * Obtain a Function value from a Module by its name.<br>
	 * The returned value corresponds to a llvm::Function value.
	 * 
	 * @see llvm::Module::getFunction()
	 */
	public Value getNamedFunction(String name) throws LLVMException {
		try {
			return new Value(LLVMGetNamedFunction(this.module,
					Pointer.pointerToCString(name)));
		} catch (IllegalArgumentException e) {
			throw new LLVMException("Function '" + name + "' not found.");
		}
	}

	/**
	 * Obtain an iterator to the first Function in a Module.
	 * 
	 * @see llvm::Module::begin()
	 */
	public Value getFirstFunction() throws LLVMException {
		try {
			return new Value(LLVMGetFirstFunction(this.module));
		} catch (IllegalArgumentException e) {
			throw new LLVMException(
					"The module does not declare any functions.");
		}
	}

	/**
	 * Obtain an iterator to the last Function in a Module.
	 * 
	 * @see llvm::Module::end()
	 */
	public Value getLastFunction() throws LLVMException {
		try {
			return new Value(LLVMGetLastFunction(this.module));
		} catch (IllegalArgumentException e) {
			throw new LLVMException(
					"The module does not declare any functions.");
		}
	}

}
