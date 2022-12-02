const text = await Deno.readTextFile("./inputs/1.txt");

const productsByElf = [];

let products: number[] = [];
for (const data of text.split("\n")) {
  if (data === "") {
    productsByElf.push(products);
    products = [];
    continue;
  }

  const product = Number.parseInt(data);
  products.push(product);
}

const caloriesByElf = productsByElf.map((products) =>
  products.reduce((sum, calories) => sum + calories, 0)
);

const sortedCaloriesSum = [...caloriesByElf].sort((a, b) => b - a);

const topTree = [
  sortedCaloriesSum[0],
  sortedCaloriesSum[1],
  sortedCaloriesSum[2],
];
const topTreeSum = topTree.reduce((sum, calories) => sum + calories, 0);
console.log({ topTree, topTreeSum });
