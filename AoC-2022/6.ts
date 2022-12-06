type MaybeStartOfPacketMarker = string;

function isStartOfPacketMarker(maybeSOPM: MaybeStartOfPacketMarker): boolean {
  return (new Set(maybeSOPM)).size === maybeSOPM.length;
}

const soughtLength = 14;

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

  for (let i = 0; i < dataStream.length - soughtLength - 1; i++) {
    const maybeSOPM = dataStream.slice(i, i + soughtLength);
    if (isStartOfPacketMarker(maybeSOPM)) {
      result = i + soughtLength;
      break;
    }
  }
  console.log({ result });
}

main();
