type Compartment = Array<string>;

function makeCompartments(rawRucksacks: string): Array<Compartment> {
  const middleIdx = rawRucksacks.length / 2;
  const firstRawCompartment = rawRucksacks.slice(0, middleIdx);
  const secondRawCompartment = rawRucksacks.slice(
    middleIdx,
    rawRucksacks.length,
  );
  return [firstRawCompartment.split(""), secondRawCompartment.split("")];
}

function makeSharedItems(compartments: Compartment[]): Set<string> {
  if (compartments.length === 0) {
    return new Set();
  }
  const sharedItems = new Set(compartments[0]);

  for (let i = 1; i < compartments.length; i++) {
    const compartmentsIAsSet = new Set(compartments[i]);
    for (const sharedItem of sharedItems) {
      if (!compartmentsIAsSet.has(sharedItem)) {
        sharedItems.delete(sharedItem);
      }
    }
  }

  return sharedItems;
}

function findSharedItem(compartments: Compartment[]): string {
  const sharedItems = makeSharedItems(compartments);
  if (sharedItems.size !== 1) {
    throw Error("Exist more than one item");
  }

  return sharedItems.values().next().value;
}

function makePrioritizeValue(str: string): number {
  if ("a" <= str && str <= "z") {
    return (str.codePointAt(0) as number) - ("a".codePointAt(0) as number) + 1;
  }
  if ("A" <= str && str <= "Z") {
    return (str.codePointAt(0) as number) - ("A".codePointAt(0) as number) + 27;
  }
  throw new Error("Bug in makePrioritizeValue");
}

async function main() {
  const text = await Deno.readTextFile("./inputs/3.txt");
  const rawRucksacks = text.split("\n");
  rawRucksacks.pop(); // last row is empty

  const compartmentsByIdx = rawRucksacks.map(makeCompartments);
  const sharedItemByIdx = compartmentsByIdx.map(findSharedItem);
  const priorityByIdx = sharedItemByIdx.map(makePrioritizeValue);
  const prioritiesSum = priorityByIdx.reduce((sum, value) => sum + value, 0);

  console.log({
    priorityByIdx: priorityByIdx.slice(0, 10),
    sharedItemByIdx: sharedItemByIdx.slice(0, 10),
    prioritiesSum
  });
}

main();
