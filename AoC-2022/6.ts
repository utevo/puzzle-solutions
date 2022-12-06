type MaybeStartOfPacketMarker = string;

function isStartOfPacketMarker(maybeSOPM: MaybeStartOfPacketMarker): boolean {
  return (new Set(maybeSOPM)).size === maybeSOPM.length;
}

async function main() {
  const text = await Deno.readTextFile("./inputs/6.txt");
  const dataStream = text.trimEnd();
  console.log({
    text,
    textLength: text.length,
    dataStream,
    dataStreamLength: dataStream.length,
  });

  let result;

  for (let i = 0; i < dataStream.length - 3; i++) {
    const maybeSOPM = dataStream.slice(i, i + 4);
    if (isStartOfPacketMarker(maybeSOPM)) {
      result = i + 4;
      break;
    }
  }
  console.log({ result });
}

main();
