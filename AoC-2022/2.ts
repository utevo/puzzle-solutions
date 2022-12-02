enum Shape {
  Rock,
  Paper,
  Scissors,
}

enum FightResult {
  LeftWon,
  Draw,
  RightWon,
}

function makeFightResult(left: Shape, right: Shape): FightResult {
  switch (left) {
    case Shape.Rock:
      return {
        [Shape.Rock]: FightResult.Draw,
        [Shape.Paper]: FightResult.RightWon,
        [Shape.Scissors]: FightResult.LeftWon,
      }[right];
    case Shape.Paper:
      return {
        [Shape.Rock]: FightResult.LeftWon,
        [Shape.Paper]: FightResult.Draw,
        [Shape.Scissors]: FightResult.RightWon,
      }[right];
    case Shape.Scissors:
      return {
        [Shape.Rock]: FightResult.RightWon,
        [Shape.Paper]: FightResult.LeftWon,
        [Shape.Scissors]: FightResult.Draw,
      }[right];
  }
}

function makeFightScore(oponent: Shape, you: Shape): number {
  const scoreByShape = makeScoreByShape(you);
  const scoreByFightResult = {
    [FightResult.LeftWon]: 0,
    [FightResult.Draw]: 3,
    [FightResult.RightWon]: 6,
  }[makeFightResult(oponent, you)];

  return scoreByShape + scoreByFightResult;
}

function makeScoreByShape(shape: Shape): number {
  return {
    [Shape.Paper]: 2,
    [Shape.Rock]: 1,
    [Shape.Scissors]: 3,
  }[shape];
}

type EncryptedStrategyGuide = EncryptedStrategyGuideRow[];
type EncryptedStrategyGuideRow = {
  oponentShape: EncryptedOponentShape;
  fightResult: EncryptedFightResult;
};

enum EncryptedOponentShape {
  A = "A",
  B = "B",
  C = "C",
}

enum EncryptedFightResult {
  X = "X",
  Y = "Y",
  Z = "Z",
}

type StrategyGuide = StrategyGuideRow[];
type StrategyGuideRow = {
  oponentShape: Shape;
  fightResult: FightResult;
};

function decryptStrategyGuide(
  encryptedStrategyGuide: EncryptedStrategyGuide,
): StrategyGuide {
  return encryptedStrategyGuide.map(decryptStrategyGuideRow);
}

function decryptStrategyGuideRow(
  encryptedStrategyGuideRow: EncryptedStrategyGuideRow,
): StrategyGuideRow {
  return {
    oponentShape: decryptOponentShape(encryptedStrategyGuideRow.oponentShape),
    fightResult: decryptFightResult(encryptedStrategyGuideRow.fightResult),
  };
}

function makeYourShape(oponentShape: Shape, fightResult: FightResult): Shape {
  switch (oponentShape) {
    case Shape.Paper:
      return {
        [FightResult.LeftWon]: Shape.Rock,
        [FightResult.Draw]: Shape.Paper,
        [FightResult.RightWon]: Shape.Scissors,
      }[fightResult];
    case Shape.Rock:
      return {
        [FightResult.LeftWon]: Shape.Scissors,
        [FightResult.Draw]: Shape.Rock,
        [FightResult.RightWon]: Shape.Paper,
      }[fightResult];
    case Shape.Scissors:
      return {
        [FightResult.LeftWon]: Shape.Paper,
        [FightResult.Draw]: Shape.Scissors,
        [FightResult.RightWon]: Shape.Rock,
      }[fightResult];
  }
}

function decryptOponentShape(
  encryptedOponentShape: EncryptedOponentShape,
): Shape {
  return {
    [EncryptedOponentShape.A]: Shape.Rock,
    [EncryptedOponentShape.B]: Shape.Paper,
    [EncryptedOponentShape.C]: Shape.Scissors,
  }[encryptedOponentShape];
}

function decryptFightResult(
  encryptedFightResult: EncryptedFightResult,
): FightResult {
  return {
    [EncryptedFightResult.X]: FightResult.LeftWon,
    [EncryptedFightResult.Y]: FightResult.Draw,
    [EncryptedFightResult.Z]: FightResult.RightWon,
  }[encryptedFightResult];
}

function parseRawEncryptedStrategyGuideRow(
  str: string,
): EncryptedStrategyGuideRow {
  const [oponentShape, fightResult] = str.split(" ");
  return {
    oponentShape: oponentShape as EncryptedOponentShape,
    fightResult: fightResult as EncryptedFightResult,
  };
}

async function main() {
  const rawData = await Deno.readTextFile("./inputs/2.txt");

  const rawRows = rawData.split("\n");
  rawRows.pop();

  const encryptedStrategyGuide = rawRows.map(parseRawEncryptedStrategyGuideRow);
  const strategyGuide = decryptStrategyGuide(encryptedStrategyGuide);

  const scoresByFightId = strategyGuide.map(({ oponentShape, fightResult }) =>
    makeFightScore(oponentShape, makeYourShape(oponentShape, fightResult))
  );
  console.log({ scoresByFightId });
  const sumOfScores = scoresByFightId.reduce((sum, score) => sum + score, 0);

  console.log({ sumOfScores });
}

main();
