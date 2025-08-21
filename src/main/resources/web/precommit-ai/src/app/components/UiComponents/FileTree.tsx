import React from "react";
import {buildFileTree, renderTree} from '../../util/util'

type Props = {
  paths: string[];
};

const FileTree: React.FC<Props> = ({ paths }) => {
  const tree = buildFileTree(paths);
  const treeText = renderTree(tree);

  return (
    <pre style={{ fontFamily: "monospace", whiteSpace: "pre" }}>
      {treeText}
    </pre>
  );
};

export default FileTree;
