let debugCounter = 0;

function debug(data: any, promptMessage: string = "Go next") {
  console.log(JSON.stringify(data, undefined, 2));
  prompt(`${promptMessage} ${String(debugCounter++)}`);
}

type Filesystem = {
  root: Directory;
  parentByFile: Map<File, Directory>;
};

type File = PlainData | Directory;

type PlainData = {
  _tag: "PlainData";
  name: string;
  size: number;
};

type Directory = {
  _tag: "Directory";
  name: string;
  files: File[];
};

type Input = {
  commands: Command[];
};

type Command = CdCommand | LsCommand;

type CdCommand = {
  _tag: "Cd";
  path: string;
};

type LsCommand = {
  _tag: "Ls";
  files: LsFile[];
};

type LsFile = LsPlainData | LsDir;

type LsPlainData = PlainData;

type LsDir = Omit<Directory, "files">;

function inputFromText(lines: string[]): Input {
  const commands: Command[] = [];
  const linesInter = lines.values();
  let currLine = linesInter.next();
  while (true) {
    if (currLine.done) {
      break;
    }
    const [_, commandName, maybeParameter] = currLine.value.split(" ");
    if (commandName === "cd") {
      const command: CdCommand = { _tag: "Cd", path: maybeParameter };
      commands.push(command);
      currLine = linesInter.next();
      continue;
    }
    if (commandName === "ls") {
      const files: LsFile[] = [];
      while (true) {
        currLine = linesInter.next();
        if (currLine.done) {
          break;
        }
        if (currLine.value[0] === "$") {
          break;
        }

        const [left, right] = currLine.value.split(" ");
        if (left === "dir") {
          files.push({ _tag: "Directory", name: right });
        } else {
          files.push({
            _tag: "PlainData",
            name: right,
            size: Number(left),
          });
        }
      }
      commands.push({ _tag: "Ls", files });
      continue;
    }

    throw new Error(`Unexpected command name ${commandName}`);
  }

  return { commands };
}

function lsFileToFile(lsFile: LsFile): File {
  if (lsFile._tag === "PlainData") {
    return lsFile;
  }

  return { ...lsFile, files: [] };
}

type SizeByDir = Map<Directory, number>;

function sizeByDirFromFilesystem(filesystem: Filesystem): SizeByDir {
  const sizeByDir: SizeByDir = new Map();
  function recurrent(file: File): number {
    if (file._tag === "PlainData") {
      return file.size;
    }

    let size = 0;
    for (const childFile of file.files) {
      size += recurrent(childFile);
    }

    sizeByDir.set(file, size);
    return size;
  }
  recurrent(filesystem.root);

  return sizeByDir;
}

function filesystemFromInput(input: Input): Filesystem {
  const root: Directory = { _tag: "Directory", name: "/", files: [] };
  const parentByFile = new Map<File, Directory>();
  const processedDirs = new Set<Directory>();

  let currDir = root;
  for (let i = 1; i < input.commands.length; i++) {
    const command = input.commands[i];
    if (command._tag === "Cd") {
      switch (command.path) {
        case "/":
          currDir = root;
          break;
        case "..": {
          // debug({ parentByFile }, "parentByFile");
          const maybeCurrDir = parentByFile.get(currDir);
          if (maybeCurrDir === undefined) {
            throw new Error(`${currDir.name} don't have parent`);
          }
          currDir = maybeCurrDir;
          break;
        }
        default: {
          const maybeCurrDir = currDir.files.find((file) =>
            file.name === command.path
          );
          if (maybeCurrDir === undefined) {
            throw new Error(`${command.path} don't exist in ${currDir.name}`);
          }
          currDir = maybeCurrDir as Directory;
        }
      }
    } else { // LS
      if (!processedDirs.has(currDir)) {
        for (const lsFile of command.files) {
          const file = lsFileToFile(lsFile);
          // debug({ file });
          parentByFile.set(file, currDir);
          currDir.files.push(file);
        }
        processedDirs.add(currDir);
      }
    }
  }

  return { root, parentByFile };
}

async function main() {
  // parse
  const text = await Deno.readTextFile("./inputs/7.txt");
  const lines = text.split("\n");
  lines.pop();
  const input = inputFromText(lines);

  const filesystem = filesystemFromInput(input);
  const sizeByDir = sizeByDirFromFilesystem(filesystem);

  const neededSpace = 30000000;
  const freeSpace = 70000000 - (sizeByDir.get(filesystem.root) as number);
  const neededToDelete = neededSpace - freeSpace;
  console.log({ neededToDelete });

  const sizeByDirFiltered: SizeByDir = new Map();
  for (const [dir, size] of sizeByDir.entries()) {
    if (size >= neededToDelete) {
      sizeByDirFiltered.set(dir, size);
    }
  }

  const sizeByDirFilteredSorted = [...sizeByDirFiltered.entries()].sort((
    [_, aSize],
    [__, bSize],
  ) => aSize - bSize);

  console.log({
    sizeByDirFiltered,
    sizeByDirFilteredSorted,
    result: sizeByDirFilteredSorted[0],
  });
}

await main();
