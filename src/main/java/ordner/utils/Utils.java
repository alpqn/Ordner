package ordner.utils;

import javafx.scene.Node;

public final class Utils
{
	public static void changeVisibility(Node node, boolean state)
	{
		node.setVisible(state);
		node.setManaged(state);
	}

	public static boolean isAlphanumeric(String str)
	{
		return str.matches("[\\w]+");
	}

	public static String simplified(String str)
	{
		return str.trim().replaceAll("\\s+", " ");
	}
}
