export default class Connection {
  constructor(context, parentNode, childNode) {
    this.context = context;
    this.parentNode = parentNode;
    this.childNode = childNode;
    this.childNode.connection = this;
  }

  drawPath() {
    if (!this.path) {
      const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
      this.context.paper.appendChild(path);
      this.path = path;
      path.setAttribute('fill', 'none');
      path.setAttribute('stroke', '#787878');
      path.setAttribute('stroke-width', '1');
    }

    this.path.setAttribute('d', this.buildD());
  }

  buildD() {
    this.startX = this.parentNode.x + this.parentNode.w - 10;
    this.startY = this.parentNode.y + this.parentNode.h / 2;
    this.endX = this.childNode.x;
    this.endY = this.childNode.y + this.childNode.h / 2;
    return "M" + this.startX + "," + this.startY + " C" + this.startX + "," + this.endY + "," + this.startX + "," + this.endY + "," + this.endX + "," + this.endY;
  }
}
