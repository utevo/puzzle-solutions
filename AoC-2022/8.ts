function isTreeVisible(
  treeMap: number[][],
  position: [y: number, x: number],
): boolean {
  const [thisY, thisX] = position;
  const m = treeMap.length;
  const n = treeMap[0].length;
  const thisTreeHeight = treeMap[thisY][thisX];

  function isVisibleFromTop(): boolean {
    if (thisY === 0) {
      return true;
    }

    for (let yi = 0; yi < thisY; yi++) {
      if (treeMap[yi][thisX] >= thisTreeHeight) {
        return false;
      }
    }

    return true;
  }

  function isVisibleFromBot(): boolean {
    if (thisY === m - 1) {
      return true;
    }

    for (let yi = thisY + 1; yi < m; yi++) {
      if (treeMap[yi][thisX] >= thisTreeHeight) {
        return false;
      }
    }

    return true;
  }

  function isVisibleFromLeft(): boolean {
    if (thisX === 0) {
      return true;
    }

    for (let xi = 0; xi < thisX; xi++) {
      if (treeMap[thisY][xi] >= thisTreeHeight) {
        return false;
      }
    }

    return true;
  }

  function isVisibleFromRight(): boolean {
    if (thisX === n - 1) {
      return true;
    }

    for (let xi = thisX + 1; xi < n; xi++) {
      if (treeMap[thisY][xi] >= thisTreeHeight) {
        return false;
      }
    }

    return true;
  }

  return isVisibleFromLeft() || isVisibleFromRight() || isVisibleFromTop() ||
    isVisibleFromBot();
}

function makeTreeScore(
  treeMap: number[][],
  position: [y: number, x: number],
): number {
  const [thisY, thisX] = position;
  const m = treeMap.length;
  const n = treeMap[0].length;
  const thisTreeHeight = treeMap[thisY][thisX];

  function makeTreeScoreTop(): number {
    let score = 0;

    for (let yi = thisY - 1; yi >= 0; yi--) {
      if (treeMap[yi][thisX] >= thisTreeHeight) {
        return score + 1;
      }
      score++;
    }

    return score;
  }

  function makeTreeScoreBot(): number {
    let score = 0;

    for (let yi = thisY + 1; yi < n; yi++) {
      if (treeMap[yi][thisX] >= thisTreeHeight) {
        return score + 1;
      }
      score++;
    }

    return score;
  }

  function makeTreeScoreLeft(): number {
    let score = 0;

    for (let xi = thisX - 1; xi >= 0; xi--) {
      if (treeMap[thisY][xi] >= thisTreeHeight) {
        return score + 1;
      }
      score++;
    }

    return score;
  }

  function makeTreeScoreRight(): number {
    let score = 0;

    for (let xi = thisX + 1; xi < n; xi++) {
      if (treeMap[thisY][xi] >= thisTreeHeight) {
        return score + 1;
      }
      score++;
    }

    return score;
  }

  return makeTreeScoreTop() * makeTreeScoreBot() * makeTreeScoreLeft() *
    makeTreeScoreRight();
}

async function main() {
  const text = await Deno.readTextFile("./inputs/8.txt");
  const textRows = text.split("\n");
  textRows.pop();

  const treeMap = textRows.map((textRow) => textRow.split("").map(Number));
  const m = treeMap.length;
  const n = treeMap[0]?.length ?? 0;

  let visibleTrees = 0;
  for (let yi = 0; yi < m; yi++) {
    for (let xi = 0; xi < n; xi++) {
      visibleTrees += isTreeVisible(treeMap, [yi, xi]) ? 1 : 0;
    }
  }

  let maxTreeScore = 0;
  for (let yi = 0; yi < m; yi++) {
    for (let xi = 0; xi < n; xi++) {
      maxTreeScore = Math.max(maxTreeScore, makeTreeScore(treeMap, [yi, xi]));
    }
  }

  console.log({ treeMap, m, n, visibleTrees, maxTreeScore });
}

await main();
