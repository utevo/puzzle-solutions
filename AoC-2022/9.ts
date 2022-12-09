function clamp(min: number, value: number, max: number): number {
  if (value < min) {
    return min;
  }
  if (value > max) {
    return max;
  }
  return value;
}

class Position {
  private constructor(public y: number, public x: number) {}
  static of(input: { y: number; x: number }): Position {
    return new Position(input.y, input.x);
  }
}

enum HeadMoveDirection {
  Up = "U",
  Down = "D",
  Left = "L",
  Right = "R",
}

class State {
  private constructor(
    public headPosition: Position,
    public tailPositions: Position[],
  ) {}

  static of(
    input: {
      headPosition: Position;
      tailPositions: Position[];
    },
  ): State {
    return new State(input.headPosition, input.tailPositions);
  }

  changed(headMove: HeadMoveDirection): State {
    const newHeadPosition = makeHeadPositionAfterMove(
      this.headPosition,
      headMove,
    );
    const newTailPositions = [...this.tailPositions];
    newTailPositions[0] = makeTailPositionAfterComing(
      newHeadPosition,
      this.tailPositions[0],
    );
    for (let i = 1; i < this.tailPositions.length; i++) {
      newTailPositions[i] = makeTailPositionAfterComing(
        newTailPositions[i - 1],
        this.tailPositions[i],
      );
    }

    return State.of({
      headPosition: newHeadPosition,
      tailPositions: newTailPositions,
    });
  }
}

function makeHeadPositionAfterMove(
  currPosition: Position,
  move: HeadMoveDirection,
): Position {
  switch (move) {
    case HeadMoveDirection.Up:
      return Position.of({ ...currPosition, y: currPosition.y + 1 });
    case HeadMoveDirection.Down:
      return Position.of({ ...currPosition, y: currPosition.y - 1 });
    case HeadMoveDirection.Left:
      return Position.of({ ...currPosition, x: currPosition.x - 1 });
    case HeadMoveDirection.Right:
      return Position.of({ ...currPosition, x: currPosition.x + 1 });
  }
}

enum PositionsDistance {
  Zero,
  One,
  TwoOrMore,
}

function makePositionDistance(
  position: Position,
  otherPosition: Position,
): PositionsDistance {
  const yDistance = Math.abs(position.y - otherPosition.y);
  const xDistance = Math.abs(position.x - otherPosition.x);

  if (yDistance === 1 && xDistance === 1) {
    return PositionsDistance.One;
  }
  if (xDistance + yDistance === 0) {
    return PositionsDistance.Zero;
  }
  if (xDistance + yDistance === 1) {
    return PositionsDistance.One;
  }

  return PositionsDistance.TwoOrMore;
}

function makeTailPositionAfterComing(
  headPosition: Position,
  tailPosition: Position,
): Position {
  switch (makePositionDistance(headPosition, tailPosition)) {
    case PositionsDistance.Zero:
    case PositionsDistance.One:
      return tailPosition;
    case PositionsDistance.TwoOrMore: {
      const yDiff = headPosition.y - tailPosition.y;
      const xDiff = headPosition.x - tailPosition.x;
      const newY = tailPosition.y + clamp(-1, yDiff, 1);
      const newX = tailPosition.x + clamp(-1, xDiff, 1);

      return Position.of({ y: newY, x: newX });
    }
  }
}

class Command {
  private constructor(
    public headMove: HeadMoveDirection,
    public quantity: number,
  ) {}

  static of(input: { headMove: HeadMoveDirection; quantity: number }): Command {
    return new Command(input.headMove, input.quantity);
  }
}

class PrevTailPositions {
  constructor(public rawData = new Set<string>()) {}

  add(position: Position): void {
    this.rawData.add(JSON.stringify(position));
  }
}

function parseCommand(rawCommand: string): Command {
  const [headMove, rawQuantity] = rawCommand.split(
    " ",
  ) as [HeadMoveDirection, string];
  return Command.of({ headMove, quantity: Number.parseInt(rawQuantity) });
}

const numberOfTailPositions = 9;

async function main() {
  const text = await Deno.readTextFile("./inputs/9.txt");
  const rawCommands = text.split("\n");
  rawCommands.pop();
  const commands = rawCommands.map(parseCommand);

  let state = State.of({
    headPosition: Position.of({ y: 0, x: 0 }),
    tailPositions: [...Array(numberOfTailPositions).keys()].map((_) => Position.of({ y: 0, x: 0 })),
  });

  const prevTailPositions = new PrevTailPositions();
  prevTailPositions.add(state.tailPositions[numberOfTailPositions - 1]);

  for (const command of commands) {
    for (let i = 0; i < command.quantity; i++) {
      state = state.changed(command.headMove);
      prevTailPositions.add(state.tailPositions[numberOfTailPositions - 1]);
    }
  }

  console.log({ result: prevTailPositions.rawData.size });
}

await main();
