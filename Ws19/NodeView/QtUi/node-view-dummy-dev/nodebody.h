#ifndef NODEBODY_H
#define NODEBODY_H

#include <QGraphicsItem>

class NodeBody : public QGraphicsItem
{
public:
    NodeBody(QGraphicsItem *parent, QColor _color, qreal _width, qreal _height);

    QRectF boundingRect() const override;
    void paint(QPainter *painter, const QStyleOptionGraphicsItem *option, QWidget *widget) override;

protected:

private:
    QColor color;
    qreal width;
    qreal height;
};

#endif // NODEBODY_H
