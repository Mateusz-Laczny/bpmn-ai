import React, { useEffect, useRef, useState } from "react";
import "bpmn-js/dist/assets/diagram-js.css";
import "bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css";
import useSWR from "swr";

const fetcher = (url: string) => fetch(url).then((res) => res.text());

export default function Home() {
  const [diagram, diagramSet] = useState("");
  const { data, error } = useSWR("/api/diagram", fetcher);
  const containerDivRef = useRef(null);

  useEffect(() => {
    if (data !== undefined) {
      diagramSet(data);
    }
  }, [diagram, data]);

  if (error) return <div>Failed to load</div>;

  if (diagram === "") return <div>Loading...</div>;

  const container = containerDivRef.current;

  import("bpmn-js/lib/Modeler").then((M) => {
    const Modeler = M.default;
    const modeler = new Modeler({
      container,
      keyboard: {
        bindTo: document,
      },
    });

    modeler
      .importXML(diagram)
      .then(({ warnings }) => {
        if (warnings.length) {
          console.log("Warnings", warnings);
        }
      })
      .catch((err) => {
        console.log("error", err.message);
      });
  });

  return (
    <main>
      <h1>Input description</h1>
      <input></input>
      <h1>Result model</h1>
      <div
        ref={containerDivRef}
        style={{
          border: "1px solid #000000",
          height: "90vh",
          width: "90vw",
          margin: "auto",
        }}
      ></div>
    </main>
  );
}
