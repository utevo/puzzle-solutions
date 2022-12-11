type Stack = string[];
type State = Array<Stack>;

function makeStack(rawStack: string): Stack {
  return rawStack.split("").reverse();
}

type Move = {
  quantity: number;
  from: number;
  to: number;
};

function makeMove(rawMove: string): Move {
  const [quantityAsString, fromAsString, toAsString] = rawMove.split(" ");
  return {
    quantity: Number.parseInt(quantityAsString),
    from: Number.parseInt(fromAsString),
    to: Number.parseInt(toAsString),
  };
}

function runMove(state: State, move: Move) {
  const moved = state[move.from - 1].splice(state[move.from - 1].length - move.quantity, move.quantity);
  state[move.to - 1].push(...moved)
}

type Operation = Pick<Move, "from" | "to">;

// function runSingleOperation(state: State, operation: Operation) {
//   const fromStack = state[operation.from - 1];
//   const toStack = state[operation.to - 1];

//   const movedItem = fromStack.pop();
//   if (movedItem === undefined) {
//     throw new Error();
//   }

//   toStack.push(movedItem);
// }

function makeOutput(state: State): string {
  return state.flatMap((stack) => stack.at(-1) ?? []).join("");
}

async function main() {
  const initStateText = await Deno.readTextFile("./inputs/5-init-state.txt");
  const rawInitStateText = initStateText.split("\n");
  rawInitStateText.pop();
  const state: State = rawInitStateText.map(makeStack);

  const movesText = await Deno.readTextFile("./inputs/5-moves.txt");
  const rawMoves = movesText.split("\n");
  rawMoves.pop();
  const moves = rawMoves.map(makeMove);

  for (const move of moves) {
    runMove(state, move);
  }

  const output = makeOutput(state);
  console.log(output)
}

main();
