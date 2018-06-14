package mobile.media.abnormalalpaca.feature;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

/**
 * The parser is based on the Shunting-yard algorithm
 * 
 * @author Gyu Jin Choi <paganinist@gmail.com>
 * @see //http://en.wikipedia.org/wiki/Shunting-yard_algorithm
 *
 */

public class Parser {

	public LinkedList<String> stringToQueue(String inputString) {
		LinkedList<String> inputStack = new LinkedList<String>();

		int len = inputString.length();
		for (int i = 0; i < len; i++) {
			inputStack.add(String.valueOf(inputString.charAt(i)));
		}

		return inputStack;
	}

	public void printOutput(Queue<String> stuff){
		StringBuilder string = new StringBuilder();
//		stuff.forEach(c -> {
//			string.append(c);
//		});
		System.out.println(string.toString());
	}

	public LinkedList<String> parse(LinkedList<String> incomingInput){
		LinkedList<String> output = new LinkedList<String>();
		Stack<String> stack = new Stack<String>();
		LinkedList<String> input = (LinkedList<String>) incomingInput.clone();
        String token = "";
		while (!input.isEmpty()){

		    String old_token = token;
			token = input.remove();

			if (Checker.isNumeric(token)){
			    if(!output.isEmpty() && Checker.isNumeric(output.getLast()) && Checker.isNumeric(old_token))
			        output.add(String.valueOf(Integer.parseInt(output.removeLast()) * 10 + Integer.parseInt(token)));
                else
				    output.add(token);
			} else if (Checker.isFunction(token)){
				stack.push(token);
			} else if (Checker.isFunctionSeparator(token)){
				while (!stack.isEmpty() &&
						!stack.peek().equals("(")){
					output.add(stack.pop());
				}
				if (stack.peek().equals("(")){

				} else {
					System.err.println("The separator or parentheses were misplaced.");
                    return null;
				}
			} else if (Checker.isOperator(token)){
				while (!stack.isEmpty() && Checker.isOperator(stack.peek()) &&
						((Checker.isLeftAssociative(token) && (Checker.getPrecedence(token) <= Checker.getPrecedence(stack.peek())) || 
								(!Checker.isLeftAssociative(token) && (Checker.getPrecedence(token) < Checker.getPrecedence(stack.peek())))))
						){
					output.add(stack.pop());
				}
				stack.push(token);
			} else if (token.equals("(")){
				stack.push(token);
			} else if (token.equals(")")){
				while (!stack.isEmpty() && !stack.peek().equals("(")){
					output.add(stack.pop());
				}
				if (stack.peek().equals("(")){
					stack.pop();
					if (!stack.isEmpty() && Checker.isFunction(stack.peek())){
						output.add(stack.pop());
					}
				} else {
					System.err.println("There are mismatched parentheses.");
                    return null;
				}

			}
		}

		if (input.isEmpty()){
			while (!stack.isEmpty()){
				if (stack.peek().equals("(") || stack.peek().equals(")")){
					System.err.println("There are mismatched parentheses.");
                    return null;
				}
				output.add(stack.pop());
			}
		}
		return output;
	}


}