package net.gjerull.etherpad.client;

import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.java.annotation.GraphWalker;

@GraphWalker(value = "quick_random(edge_coverage(100))", start = "init")
public class EPLiteClientGraphTest extends ExecutionContext implements Graph {

	@Override
	public void init() {
		// TODO Auto-generated method stub
		System.out.println("iniciase.");

	}

	@Override
	public void e_GetText() {
		// TODO Auto-generated method stub
		System.out.println("getText.");

	}

	@Override
	public void e_DeletePad() {
		// TODO Auto-generated method stub
		System.out.println("borrar Pad.");

	}

	@Override
	public void e_setup() {
		// TODO Auto-generated method stub
		System.out.println("Volviendo al pad.");

	}

	@Override
	public void e_CreatePad() {
		// TODO Auto-generated method stub
		System.out.println("Crear Pad.");

	}

	@Override
	public void v_ExistePad() {
		// TODO Auto-generated method stub
		System.out.println("O pad existe.");

	}

	@Override
	public void e_SetText() {
		// TODO Auto-generated method stub
		System.out.println("setea texto.");

	}

	@Override
	public void v_NoExistePad() {
		// TODO Auto-generated method stub
		System.out.println("o pad non existe.");

	}

}
