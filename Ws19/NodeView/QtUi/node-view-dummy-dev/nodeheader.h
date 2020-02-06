#ifndef NODEHEADER_H
#define NODEHEADER_H

#include <QGraphicsItem>

class NodeHeader : public QGraphicsItem
{
public:
    NodeHeader(QGraphicsItem *parent = nullptr,
               QString _name = "-",
               QColor _text_color = Qt::white,
               qreal _width = 200,
               qreal _height = 30);

    QRectF boundingRect() const override;
    void paint(QPainter* painter, const QStyleOptionGraphicsItem *option, QWidget *widget) override;

private:
    QColor text_color;
    QString name;
    qreal width;
    qreal height;
    qreal offset_x;
    qreal offset_y;
};

#endif // NODEHEADER_H
