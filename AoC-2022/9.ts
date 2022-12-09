import { clamp } from "./common.ts";

class Position {
  private constructor(public y: number, public x: number) {}
  static of(input: { y: number; x: number }): Position {
    return new Position(input.y, input.x);
  }

  moved(direction: MoveDirection): Position {
    switch (direction) {
      case MoveDirection.Up:
        return Position.of({ ...this, y: this.y + 1 });
      case MoveDirection.Down:
        return Position.of({ ...this, y: this.y - 1 });
      case MoveDirection.Left:
        return Position.of({ ...this, x: this.x - 1 });
      case MoveDirection.Right:
        return Position.of({ ...this, x: this.x + 1 });
    }
  }

  attractedBy(somePosition: Position): Position {
    switch (PositionsDistance.between(somePosition, this).value) {
      case PositionsDistanceV.Zero:
      case PositionsDistanceV.One:
        return this;
      case PositionsDistanceV.TwoOrMore: {
        const yDiff = somePosition.y - this.y;
        const xDiff = somePosition.x - this.x;
        const newY = this.y + clamp(-1, yDiff, 1);
        const newX = this.x + clamp(-1, xDiff, 1);

        return Position.of({ y: newY, x: newX });
      }
    }
  }
}

enum MoveDirection {
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

  afterHeadMove(headMoveDirection: MoveDirection): State {
    const newHeadPosition = this.headPosition.moved(headMoveDirection);
    const newTailPositions = [...this.tailPositions];
    newTailPositions[0] = this.tailPositions[0].attractedBy(newHeadPosition);
    for (let i = 1; i < this.tailPositions.length; i++) {
      newTailPositions[i] = this.tailPositions[i].attractedBy(
        newTailPositions[i - 1],
      );
    }

    return State.of({
      headPosition: newHeadPosition,
      tailPositions: newTailPositions,
    });
  }
}

class PositionsDistance {
  private constructor(public value: PositionsDistanceV) {}

  static between(
    position: Position,
    otherPosition: Position,
  ): PositionsDistance {
    const yDistance = Math.abs(position.y - otherPosition.y);
    const xDistance = Math.abs(position.x - otherPosition.x);

    if (yDistance === 1 && xDistance === 1) {
      return new PositionsDistance(PositionsDistanceV.One);
    }
    if (xDistance + yDistance === 0) {
      return new PositionsDistance(PositionsDistanceV.Zero);
    }
    if (xDistance + yDistance === 1) {
      return new PositionsDistance(PositionsDistanceV.One);
    }

    return new PositionsDistance(PositionsDistanceV.TwoOrMore);
  }
}
enum PositionsDistanceV {
  Zero,
  One,
  TwoOrMore,
}

class Command {
  private constructor(
    public headMove: MoveDirection,
    public quantity: number,
  ) {}

  static of(input: { headMove: MoveDirection; quantity: number }): Command {
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
  ) as [MoveDirection, string];
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
    tailPositions: [...Array(numberOfTailPositions).keys()].map((_) =>
      Position.of({ y: 0, x: 0 })
    ),
  });

  const prevTailPositions = new PrevTailPositions();
  prevTailPositions.add(state.tailPositions[numberOfTailPositions - 1]);

  for (const command of commands) {
    for (let i = 0; i < command.quantity; i++) {
      state = state.afterHeadMove(command.headMove);
      prevTailPositions.add(state.tailPositions[numberOfTailPositions - 1]);
    }
  }

  console.log({ result: prevTailPositions.rawData.size });
}

await main();
