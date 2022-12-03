type Items = Array<string>;

// function makeCompartments(rawRucksack: string): Array<Items> {
//   const middleIdx = rawRucksack.length / 2;
//   const firstRawCompartment = rawRucksack.slice(0, middleIdx);
//   const secondRawCompartment = rawRucksack.slice(
//     middleIdx,
//     rawRucksack.length,
//   );
//   return [firstRawCompartment.split(""), secondRawCompartment.split("")];
// }

type Group = Items[];

function makeGroups(rawRucksacks: string[]): Group[] {
  const groups = [];
  for (let i = 0; (i + 2) < rawRucksacks.length; i += 3) {
    const group: Group = [
      rawRucksacks[i + 0].split(""),
      rawRucksacks[i + 1].split(""),
      rawRucksacks[i + 2].split(""),
    ];
    groups.push(group);
  }

  return groups;
}

function makeSharedItems(compartments: Items[]): Set<string> {
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

function findSharedItem(compartments: Items[]): string {
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

  const groups = makeGroups(rawRucksacks);
  const sharedItemByIdx = groups.map(findSharedItem);
  const priorityByIdx = sharedItemByIdx.map(makePrioritizeValue);
  const prioritiesSum = priorityByIdx.reduce((sum, value) => sum + value, 0);

  console.log({
    priorityByIdx: priorityByIdx.slice(0, 10),
    sharedItemByIdx: sharedItemByIdx.slice(0, 10),
    prioritiesSum,
  });
}

main();
