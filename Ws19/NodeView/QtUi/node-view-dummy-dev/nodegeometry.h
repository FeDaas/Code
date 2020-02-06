#ifndef NODEGEOMETRY_H
#define NODEGEOMETRY_H

#include <QGraphicsItem>

class NodeBody;
class NodeHeader;

class NodeGeometry : public QGraphicsItem
{
public:
    enum { Type = UserType + 2 };
    NodeGeometry(const QString _name,
                 bool *snap,
                 QColor _color = Qt::gray,
                 qreal minimal_width = 200,
                 qreal minimal_height = 100,
                 int inputs = 1,
                 int outputs = 1);

    ~NodeGeometry() override;

    QRectF boundingRect() const override;
    void paint(QPainter *painter, const QStyleOptionGraphicsItem *option, QWidget *widget) override;

    qreal calculateWidth(qreal minimal_width, QString name);
    qreal calculateHeight(qreal minimal_height, int inputs, int outputs);

    void snapActivate();
    int type() const override;


protected:
    void mousePressEvent(QGraphicsSceneMouseEvent *event) override;
    void mouseReleaseEvent(QGraphicsSceneMouseEvent *event) override;

    QVariant itemChange(GraphicsItemChange change, const QVariant &value) override;

private:
    bool pressed;
    bool* snap;
    QColor color;
    QString name;

    NodeBody *body;
    NodeHeader *header;

    qreal width;
    qreal height;
};

#endif // NODEGEOMETRY_H
