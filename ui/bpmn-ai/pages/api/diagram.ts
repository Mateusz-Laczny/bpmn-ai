import path from 'path';
import { promises as fs } from 'fs';
import type { NextApiRequest, NextApiResponse } from 'next'

export default async function handler(req: NextApiRequest, res: NextApiResponse<string>) {
  const diagramsDirectory = path.join(process.cwd(), 'diagrams');
  const fileContents = await fs.readFile(diagramsDirectory + "/diagram.bpmn", 'utf-8');

  res
  .setHeader('Content-Type', 'text/xml')
  .status(200)
  .send(fileContents);
}