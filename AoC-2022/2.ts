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
  yourShape: EncryptedYourShape;
};

enum EncryptedOponentShape {
  A = "A",
  B = "B",
  C = "C",
}

enum EncryptedYourShape {
  X = "X",
  Y = "Y",
  Z = "Z",
}

type StrategyGuide = StrategyGuideRow[];
type StrategyGuideRow = {
  oponentShape: Shape;
  yourShape: Shape;
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
    yourShape: decryptYourShape(encryptedStrategyGuideRow.yourShape),
  };
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

function decryptYourShape(encryptedYourShape: EncryptedYourShape): Shape {
  return {
    [EncryptedYourShape.X]: Shape.Rock,
    [EncryptedYourShape.Y]: Shape.Paper,
    [EncryptedYourShape.Z]: Shape.Scissors,
  }[encryptedYourShape];
}

function parseRawEncryptedStrategyGuideRow(
  str: string,
): EncryptedStrategyGuideRow {
  const [oponentShape, yourShape] = str.split(" ");
  return {
    oponentShape: oponentShape as EncryptedOponentShape,
    yourShape: yourShape as EncryptedYourShape,
  };
}

async function main() {
  const rawData = await Deno.readTextFile("./inputs/2.txt");

  const rawRows = rawData.split("\n");
  rawRows.pop();

  const encryptedStrategyGuide = rawRows.map(parseRawEncryptedStrategyGuideRow);
  const strategyGuide = decryptStrategyGuide(encryptedStrategyGuide);

  const scoresByFightId = strategyGuide.map(({ oponentShape, yourShape }) =>
    makeFightScore(oponentShape, yourShape)
  );
  console.log({ scoresByFightId });
  const sumOfScores = scoresByFightId.reduce((sum, score) => sum + score, 0);

  console.log({ sumOfScores });
}

main();
